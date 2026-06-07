import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CssBaseline, ThemeProvider } from '@mui/material';
import App from './App';
import { store } from '@/store';
import { theme } from '@/theme';
import { NotificationsProvider } from '@/components/Notifications';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 10_000,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <NotificationsProvider>
            <BrowserRouter>
              <App />
            </BrowserRouter>
          </NotificationsProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </Provider>
  </React.StrictMode>,
);
