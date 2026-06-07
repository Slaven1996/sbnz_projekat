import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { Alert, Snackbar, type AlertColor } from '@mui/material';

interface NotifyState {
  open: boolean;
  message: string;
  severity: AlertColor;
}

interface NotifyContextValue {
  notify: (message: string, severity?: AlertColor) => void;
  success: (message: string) => void;
  error: (message: string) => void;
}

const NotifyContext = createContext<NotifyContextValue | null>(null);

export function NotificationsProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<NotifyState>({
    open: false,
    message: '',
    severity: 'info',
  });

  const notify = useCallback((message: string, severity: AlertColor = 'info') => {
    setState({ open: true, message, severity });
  }, []);

  const value = useMemo<NotifyContextValue>(
    () => ({
      notify,
      success: (m) => notify(m, 'success'),
      error: (m) => notify(m, 'error'),
    }),
    [notify],
  );

  return (
    <NotifyContext.Provider value={value}>
      {children}
      <Snackbar
        open={state.open}
        autoHideDuration={4000}
        onClose={() => setState((s) => ({ ...s, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          severity={state.severity}
          variant="filled"
          onClose={() => setState((s) => ({ ...s, open: false }))}
        >
          {state.message}
        </Alert>
      </Snackbar>
    </NotifyContext.Provider>
  );
}

export function useNotify(): NotifyContextValue {
  const ctx = useContext(NotifyContext);
  if (!ctx) throw new Error('useNotify must be used within NotificationsProvider');
  return ctx;
}
