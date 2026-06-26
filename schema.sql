-- ============================================
-- Family Town DB Schema
-- Supabase SQL Editor 에 붙여넣고 실행하세요
-- ============================================

-- [1] profiles (auth.users 확장)
CREATE TABLE IF NOT EXISTS public.profiles (
    id         UUID         PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    nickname   VARCHAR(20)  NOT NULL,
    avatar     VARCHAR(10)  NOT NULL DEFAULT '👤',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- [2] notes (스티커 메모)
CREATE TABLE IF NOT EXISTS public.notes (
    id        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID         NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content   TEXT         NOT NULL,
    color     VARCHAR(10)  NOT NULL DEFAULT '#F5F0E8',
    pos_x     NUMERIC(5,2) NOT NULL DEFAULT 20 CHECK (pos_x BETWEEN 0 AND 100),
    pos_y     NUMERIC(5,2) NOT NULL DEFAULT 20 CHECK (pos_y BETWEEN 0 AND 100),
    rotation  NUMERIC(4,1) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- [3] leaving_work_events (퇴근 이벤트 로그)
CREATE TABLE IF NOT EXISTS public.leaving_work_events (
    id        BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    actor_id  UUID         NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- [4] push_subscriptions (Web Push 기기 구독 정보)
CREATE TABLE IF NOT EXISTS public.push_subscriptions (
    id        UUID  PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID  NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    endpoint  TEXT  NOT NULL UNIQUE,
    p256dh    TEXT  NOT NULL,
    auth_key  TEXT  NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── 인덱스 ──
CREATE INDEX IF NOT EXISTS idx_notes_author_id   ON public.notes(author_id);
CREATE INDEX IF NOT EXISTS idx_notes_created_at  ON public.notes(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_leave_actor_id    ON public.leaving_work_events(actor_id);
CREATE INDEX IF NOT EXISTS idx_push_user_id      ON public.push_subscriptions(user_id);

-- ── updated_at 자동 갱신 트리거 ──
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$;

CREATE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

CREATE TRIGGER trg_notes_updated_at
    BEFORE UPDATE ON public.notes
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

-- ── 회원가입 시 profiles 자동 생성 ──
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
BEGIN
    INSERT INTO public.profiles (id, nickname, avatar)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'nickname', '새 가족'),
        COALESCE(NEW.raw_user_meta_data->>'avatar', '👤')
    );
    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ── Row Level Security ──
ALTER TABLE public.profiles            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notes               ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.leaving_work_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.push_subscriptions  ENABLE ROW LEVEL SECURITY;

-- profiles
CREATE POLICY "profiles: 가족 모두 조회"   ON public.profiles FOR SELECT TO authenticated USING (true);
CREATE POLICY "profiles: 본인만 수정"       ON public.profiles FOR UPDATE TO authenticated USING (auth.uid() = id);

-- notes
CREATE POLICY "notes: 가족 모두 조회"      ON public.notes FOR SELECT TO authenticated USING (true);
CREATE POLICY "notes: 본인만 등록"         ON public.notes FOR INSERT TO authenticated WITH CHECK (auth.uid() = author_id);
CREATE POLICY "notes: 본인만 삭제"         ON public.notes FOR DELETE TO authenticated USING (auth.uid() = author_id);

-- leaving_work_events
CREATE POLICY "leave: 가족 모두 조회"      ON public.leaving_work_events FOR SELECT TO authenticated USING (true);
CREATE POLICY "leave: 본인만 등록"         ON public.leaving_work_events FOR INSERT TO authenticated WITH CHECK (auth.uid() = actor_id);

-- push_subscriptions
CREATE POLICY "push: 본인 기기만 조회"     ON public.push_subscriptions FOR SELECT TO authenticated USING (auth.uid() = user_id);
CREATE POLICY "push: 본인 기기만 등록"     ON public.push_subscriptions FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "push: 본인 기기만 삭제"     ON public.push_subscriptions FOR DELETE TO authenticated USING (auth.uid() = user_id);

-- ── Realtime 활성화 ──
ALTER PUBLICATION supabase_realtime ADD TABLE public.notes;
ALTER PUBLICATION supabase_realtime ADD TABLE public.leaving_work_events;
