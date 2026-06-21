import { ReactNode, FormEventHandler } from 'react';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  type Breakpoint,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

export interface FormDialogProps {
  open: boolean;
  title: string;
  onClose: () => void;
  onSubmit: FormEventHandler<HTMLFormElement>;
  submitting?: boolean;
  submitLabel?: string;
  errorMessage?: string | null;
  maxWidth?: Breakpoint;
  children: ReactNode;
}

export function FormDialog({
  open,
  title,
  onClose,
  onSubmit,
  submitting,
  submitLabel = 'Save',
  errorMessage,
  maxWidth = 'sm',
  children,
}: FormDialogProps) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth={maxWidth} fullWidth>
      <Box component="form" onSubmit={onSubmit} noValidate>
        <DialogTitle sx={{ pr: 6 }}>
          {title}
          <IconButton
            aria-label="close"
            onClick={onClose}
            disabled={submitting}
            sx={{ position: 'absolute', right: 8, top: 8, color: 'text.secondary' }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          {errorMessage && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorMessage}
            </Alert>
          )}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>{children}</Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitLabel}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
}
