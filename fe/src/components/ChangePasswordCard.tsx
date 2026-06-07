import { Alert, Box, Button, Card, CardContent, Stack, Typography } from '@mui/material';
import LockResetIcon from '@mui/icons-material/LockReset';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation } from '@tanstack/react-query';
import { useState } from 'react';
import { changePassword } from '@/api/auth';
import { extractErrorMessage } from '@/api/axios';
import { RHFTextField } from '@/components/form/RHFTextField';
import { useNotify } from '@/components/Notifications';

const schema = z
  .object({
    oldPassword: z.string().min(1, 'Current password is required'),
    newPassword: z.string().min(6, 'New password must be at least 6 characters'),
    confirmNewPassword: z.string().min(1, 'Please confirm the new password'),
  })
  .refine((v) => v.newPassword === v.confirmNewPassword, {
    message: 'Passwords do not match',
    path: ['confirmNewPassword'],
  })
  .refine((v) => v.newPassword !== v.oldPassword, {
    message: 'New password must differ from the current one',
    path: ['newPassword'],
  });
type FormValues = z.infer<typeof schema>;

const emptyValues: FormValues = { oldPassword: '', newPassword: '', confirmNewPassword: '' };

export function ChangePasswordCard() {
  const notify = useNotify();
  const [serverError, setServerError] = useState<string | null>(null);

  const { control, handleSubmit, reset } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues,
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      changePassword({ oldPassword: values.oldPassword, newPassword: values.newPassword }),
    onSuccess: () => {
      notify.success('Password changed successfully');
      reset(emptyValues);
      setServerError(null);
    },
    onError: (err) => setServerError(extractErrorMessage(err, 'Could not change password')),
  });

  const onSubmit = (values: FormValues) => {
    setServerError(null);
    mutation.mutate(values);
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
          <LockResetIcon color="primary" />
          <Typography variant="subtitle1" fontWeight={600}>
            Change password
          </Typography>
        </Stack>

        {serverError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {serverError}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <Stack spacing={2} sx={{ maxWidth: 420 }}>
            <RHFTextField
              name="oldPassword"
              control={control}
              label="Current password"
              type="password"
              autoComplete="current-password"
            />
            <RHFTextField
              name="newPassword"
              control={control}
              label="New password"
              type="password"
              autoComplete="new-password"
            />
            <RHFTextField
              name="confirmNewPassword"
              control={control}
              label="Confirm new password"
              type="password"
              autoComplete="new-password"
            />
            <Box>
              <Button type="submit" variant="contained" disabled={mutation.isPending}>
                {mutation.isPending ? 'Updating…' : 'Update password'}
              </Button>
            </Box>
          </Stack>
        </Box>
      </CardContent>
    </Card>
  );
}
