import { ReactNode } from 'react';
import { Box, Button, Stack, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';

export interface PageHeaderProps {
  title: string;
  subtitle?: string;
  addLabel?: string;
  onAdd?: () => void;
  actions?: ReactNode;
}

export function PageHeader({ title, subtitle, addLabel, onAdd, actions }: PageHeaderProps) {
  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      justifyContent="space-between"
      alignItems={{ xs: 'flex-start', sm: 'center' }}
      spacing={2}
      sx={{ mb: 3 }}
    >
      <Box>
        <Typography variant="h5" fontWeight={600}>
          {title}
        </Typography>
        {subtitle && (
          <Typography variant="body2" color="text.secondary">
            {subtitle}
          </Typography>
        )}
      </Box>
      <Stack direction="row" spacing={1}>
        {actions}
        {onAdd && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={onAdd}>
            {addLabel ?? 'Add'}
          </Button>
        )}
      </Stack>
    </Stack>
  );
}
