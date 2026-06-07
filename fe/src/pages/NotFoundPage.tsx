import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <Box sx={{ textAlign: 'center', py: 10 }}>
      <Typography variant="h2" fontWeight={700} color="primary">
        404
      </Typography>
      <Typography variant="h6" sx={{ mb: 3 }}>
        Page not found
      </Typography>
      <Button variant="contained" onClick={() => navigate('/')}>
        Back to home
      </Button>
    </Box>
  );
}
