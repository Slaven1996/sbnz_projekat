import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Stack,
  Typography,
} from '@mui/material';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { login } from '@/api/auth';
import { extractErrorMessage } from '@/api/axios';
import { loginSuccess } from '@/store/authSlice';
import { useAppDispatch, useIsAuthenticated } from '@/store/hooks';
import { RHFTextField } from '@/components/form/RHFTextField';

const schema = z.object({
  username: z.string().email('Enter a valid e-mail'),
  password: z.string().min(1, 'Password is required'),
});
type LoginForm = z.infer<typeof schema>;

export function LoginPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const isAuthed = useIsAuthenticated();
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const { control, handleSubmit } = useForm<LoginForm>({
    resolver: zodResolver(schema),
    defaultValues: { username: '', password: '' },
  });

  if (isAuthed) {
    return <Navigate to="/" replace />;
  }

  const from = (location.state as { from?: Location })?.from?.pathname ?? '/';

  const onSubmit = async (values: LoginForm) => {
    setServerError(null);
    setSubmitting(true);
    try {
      const token = await login(values);
      dispatch(loginSuccess(token));
      navigate(from, { replace: true });
    } catch (err) {
      setServerError(extractErrorMessage(err, 'Invalid e-mail or password'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #0277bd 0%, #00838f 100%)',
        p: 2,
      }}
    >
      <Card sx={{ width: 400, maxWidth: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Stack alignItems="center" spacing={1} sx={{ mb: 3 }}>
            <WaterDropIcon color="primary" sx={{ fontSize: 48 }} />
            <Typography variant="h5" fontWeight={700}>
              Hydro Monitoring
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sign in to your account
            </Typography>
          </Stack>

          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <Stack spacing={2}>
              <RHFTextField
                name="username"
                control={control}
                label="E-mail"
                type="email"
                autoFocus
              />
              <RHFTextField name="password" control={control} label="Password" type="password" />
              <Button type="submit" variant="contained" size="large" disabled={submitting}>
                {submitting ? 'Signing in…' : 'Sign in'}
              </Button>
            </Stack>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
