import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Grid,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { Controller, useForm } from 'react-hook-form';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { Dayjs } from 'dayjs';
import { useSimulation } from '@/api/simulation';
import type { SimulationParams, StepUnit } from '@/api/types';
import { extractErrorMessage } from '@/api/axios';
import { PageHeader } from '@/components/PageHeader';
import { SimulationTimeline } from '@/components/SimulationTimeline';

interface FormValues {
  startDate: Dayjs | null;
  endDate: Dayjs | null;
  step: StepUnit;
}

const defaultValues: FormValues = {
  startDate: null,
  endDate: null,
  step: 'HOUR',
};

export function HistoricalTrendsPage() {
  const [params, setParams] = useState<SimulationParams | null>(null);
  const { control, handleSubmit, watch } = useForm<FormValues>({ defaultValues });
  const { data, isFetching, isError, error } = useSimulation(params);

  const startDate = watch('startDate');
  const endDate = watch('endDate');

  const bothSelected = Boolean(startDate && endDate);
  const rangeInvalid = bothSelected && !startDate!.isBefore(endDate!);

  const onSubmit = (v: FormValues) => {
    if (!v.startDate || !v.endDate) return;
    if (!v.startDate.isBefore(v.endDate)) return;
    setParams({
      startDate: v.startDate.format('YYYY-MM-DDTHH:mm:ss'),
      endDate: v.endDate.format('YYYY-MM-DDTHH:mm:ss'),
      step: v.step,
    });
  };

  return (
    <Box>
      <PageHeader
        title="Historical Trends Behavior"
        subtitle="Replay past sensor readings through the rule engine and watch how the inferred state evolves"
      />

      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={6} md={4}>
              <Controller
                name="startDate"
                control={control}
                render={({ field }) => (
                  <DateTimePicker
                    label="Start date"
                    value={field.value}
                    onChange={field.onChange}
                    ampm={false}
                    format="DD MMM YYYY HH:mm"
                    maxDateTime={endDate ?? undefined}
                    slotProps={{ textField: { size: 'small', fullWidth: true } }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Controller
                name="endDate"
                control={control}
                render={({ field }) => (
                  <DateTimePicker
                    label="End date"
                    value={field.value}
                    onChange={field.onChange}
                    ampm={false}
                    format="DD MMM YYYY HH:mm"
                    minDateTime={startDate ?? undefined}
                    slotProps={{ textField: { size: 'small', fullWidth: true } }}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <Controller
                name="step"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Time step" size="small" fullWidth>
                    <MenuItem value="HOUR">Hourly</MenuItem>
                    <MenuItem value="DAY">Daily</MenuItem>
                  </TextField>
                )}
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="contained"
                startIcon={<PlayArrowIcon />}
                onClick={handleSubmit(onSubmit)}
                disabled={!bothSelected || rangeInvalid || isFetching}
              >
                Run
              </Button>
            </Grid>
            {rangeInvalid && (
              <Grid item xs={12}>
                <Typography variant="caption" color="error">
                  Start date must be strictly before the end date.
                </Typography>
              </Grid>
            )}
          </Grid>
        </Paper>
      </LocalizationProvider>

      {isFetching && (
        <Stack alignItems="center" sx={{ py: 6 }}>
          <CircularProgress />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Replaying readings through the rule engine…
          </Typography>
        </Stack>
      )}

      {isError && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {extractErrorMessage(error, 'Simulation failed')}
        </Alert>
      )}

      {!isFetching && data && (
        <>
          <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={2}
              divider={<Box sx={{ borderLeft: '1px solid', borderColor: 'divider' }} />}
            >
              <SummaryStat label="Steps" value={String(data.stepCount)} />
              <SummaryStat label="Readings replayed" value={String(data.totalReadings)} />
              <SummaryStat label="Time step" value={data.stepUnit} />
              <SummaryStat label="Locations" value={data.locationsInvolved.join(', ') || '-'} />
            </Stack>
          </Paper>

          <SimulationTimeline events={data.timeline} />
        </>
      )}

      {!isFetching && !data && !isError && (
        <Typography variant="body2" color="text.secondary">
          Choose a start and end date and press <strong>Run</strong> to replay the historical
          readings.
        </Typography>
      )}
    </Box>
  );
}

function SummaryStat({ label, value }: { label: string; value: string }) {
  return (
    <Box sx={{ px: 1 }}>
      <Typography variant="h6" fontWeight={700}>
        {value}
      </Typography>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
    </Box>
  );
}
