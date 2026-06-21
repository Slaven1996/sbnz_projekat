import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { ConfirmDialog } from './ConfirmDialog';

export interface NavigationGuardConfig {
  when: boolean;
  title?: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm?: () => void | Promise<void>;
}

interface NavigationGuardContextValue {
  attemptNavigate: (navigate: () => void) => void;
  setGuard: (config: NavigationGuardConfig | null) => void;
}

const NavigationGuardContext = createContext<NavigationGuardContextValue | null>(null);

export function NavigationGuardProvider({ children }: { children: ReactNode }) {
  const guardRef = useRef<NavigationGuardConfig | null>(null);
  const pendingRef = useRef<(() => void) | null>(null);
  const [dialogConfig, setDialogConfig] = useState<NavigationGuardConfig | null>(null);
  const [loading, setLoading] = useState(false);

  const setGuard = useCallback((config: NavigationGuardConfig | null) => {
    guardRef.current = config && config.when ? config : null;
  }, []);

  const attemptNavigate = useCallback((navigate: () => void) => {
    const guard = guardRef.current;
    if (guard?.when) {
      pendingRef.current = navigate;
      setDialogConfig(guard);
    } else {
      navigate();
    }
  }, []);

  const handleCancel = useCallback(() => {
    if (loading) return;
    pendingRef.current = null;
    setDialogConfig(null);
  }, [loading]);

  const handleConfirm = useCallback(async () => {
    const guard = guardRef.current;
    const navigate = pendingRef.current;
    try {
      setLoading(true);
      await guard?.onConfirm?.();
    } finally {
      setLoading(false);
    }
    guardRef.current = null;
    pendingRef.current = null;
    setDialogConfig(null);
    navigate?.();
  }, []);

  const value = useMemo(() => ({ attemptNavigate, setGuard }), [attemptNavigate, setGuard]);

  return (
    <NavigationGuardContext.Provider value={value}>
      {children}
      <ConfirmDialog
        open={dialogConfig != null}
        title={dialogConfig?.title ?? 'Leave this page?'}
        message={dialogConfig?.message ?? 'Are you sure you want to leave this page?'}
        confirmLabel={dialogConfig?.confirmLabel ?? 'Leave'}
        cancelLabel={dialogConfig?.cancelLabel ?? 'Stay'}
        loading={loading}
        onConfirm={handleConfirm}
        onClose={handleCancel}
      />
    </NavigationGuardContext.Provider>
  );
}

export function useNavigationGuard() {
  const ctx = useContext(NavigationGuardContext);
  if (!ctx) {
    throw new Error('useNavigationGuard must be used within a NavigationGuardProvider');
  }
  return ctx;
}

export function useNavigationBlocker(config: NavigationGuardConfig) {
  const { setGuard } = useNavigationGuard();
  const onConfirmRef = useRef(config.onConfirm);
  onConfirmRef.current = config.onConfirm;

  const { when, title, message, confirmLabel, cancelLabel } = config;

  useEffect(() => {
    setGuard({
      when,
      title,
      message,
      confirmLabel,
      cancelLabel,
      onConfirm: () => onConfirmRef.current?.(),
    });
    return () => setGuard(null);
  }, [setGuard, when, title, message, confirmLabel, cancelLabel]);

  useEffect(() => {
    if (!when) return;
    const handler = (e: BeforeUnloadEvent) => {
      e.preventDefault();
      e.returnValue = '';
    };
    window.addEventListener('beforeunload', handler);
    return () => window.removeEventListener('beforeunload', handler);
  }, [when]);
}
