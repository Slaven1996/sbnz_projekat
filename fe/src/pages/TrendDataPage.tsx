import { useMemo, useState } from 'react';
import {
  Box,
  Button,
  Chip,
  FormControlLabel,
  Grid,
  Paper,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';
import type { ColumnDef } from '@tanstack/react-table';
import { useTrendDataAll, useTrendDataSearch } from '@/api/trendData';
import type { TrendDataResponse, TrendDataSearchParams } from '@/api/types';
import { PageHeader } from '@/components/PageHeader';
import { DataTable } from '@/components/DataTable';
import { useTableState } from '@/hooks/useTableState';

interface Filters {
  locationCode: string;
  tagName: string;
  startDate: string;
  endDate: string;
}

const emptyFilters: Filters = { locationCode: '', tagName: '', startDate: '', endDate: '' };

export function TrendDataPage() {
  const table = useTableState(20, [{ id: 'logTime', desc: true }]);
  const [draft, setDraft] = useState<Filters>(emptyFilters);
  const [applied, setApplied] = useState<Filters>(emptyFilters);
  const [loadAll, setLoadAll] = useState(false);

  const filterParams = {
    locationCode: applied.locationCode || undefined,
    tagName: applied.tagName || undefined,
    startDate: applied.startDate || undefined,
    endDate: applied.endDate || undefined,
  };

  const pagedParams: TrendDataSearchParams = {
    ...filterParams,
    page: table.page,
    size: table.pageSize,
    sort: table.sortParam,
  };
  const paged = useTrendDataSearch(pagedParams, !loadAll);
  const all = useTrendDataAll(filterParams, loadAll);

  const rows = loadAll ? all.data ?? [] : paged.data?.content ?? [];
  const totalElements = loadAll ? rows.length : paged.data?.totalElements ?? 0;
  const loading = loadAll ? all.isFetching : paged.isFetching;

  const columns = useMemo<ColumnDef<TrendDataResponse, any>[]>(
    () => [
      { accessorKey: 'locationCode', header: 'Location', cell: (c) => c.getValue() || '—' },
      { accessorKey: 'tagName', header: 'Tag', cell: (c) => c.getValue() || '—' },
      {
        accessorKey: 'logTime',
        header: 'Log Time',
        cell: (c) => {
          const v = c.getValue() as string | null;
          return v ? new Date(v).toLocaleString() : '—';
        },
      },
      { accessorKey: 'tagValue', header: 'Value', enableSorting: false },
      {
        accessorKey: 'valid',
        header: 'Valid',
        enableSorting: false,
        cell: (c) =>
          c.getValue() ? (
            <Chip size="small" color="success" label="Valid" />
          ) : (
            <Chip size="small" color="warning" label="Invalid" />
          ),
      },
    ],
    [],
  );

  const applyFilters = () => {
    table.setPage(0);
    setApplied(draft);
  };

  const clearFilters = () => {
    setDraft(emptyFilters);
    setApplied(emptyFilters);
    table.setPage(0);
  };

  return (
    <Box>
      <PageHeader title="Trend Data" subtitle="Historical sensor readings (read-only, filterable)" />

      <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              label="Location Code"
              size="small"
              fullWidth
              value={draft.locationCode}
              onChange={(e) => setDraft((d) => ({ ...d, locationCode: e.target.value }))}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              label="Tag Name"
              size="small"
              fullWidth
              value={draft.tagName}
              onChange={(e) => setDraft((d) => ({ ...d, tagName: e.target.value }))}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              label="Start Date"
              type="datetime-local"
              size="small"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={draft.startDate}
              onChange={(e) => setDraft((d) => ({ ...d, startDate: e.target.value }))}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              label="End Date"
              type="datetime-local"
              size="small"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={draft.endDate}
              onChange={(e) => setDraft((d) => ({ ...d, endDate: e.target.value }))}
            />
          </Grid>
          <Grid item xs={12}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={1}
              alignItems={{ xs: 'flex-start', sm: 'center' }}
              justifyContent="space-between"
            >
              <Stack direction="row" spacing={1}>
                <Button variant="contained" startIcon={<SearchIcon />} onClick={applyFilters}>
                  Search
                </Button>
                <Button variant="outlined" startIcon={<ClearIcon />} onClick={clearFilters}>
                  Clear
                </Button>
              </Stack>
              <FormControlLabel
                control={
                  <Switch checked={loadAll} onChange={(e) => setLoadAll(e.target.checked)} />
                }
                label="Load all (no pagination)"
              />
            </Stack>
          </Grid>
        </Grid>
      </Paper>

      {loadAll && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          Showing all {totalElements} matching record{totalElements === 1 ? '' : 's'} (ordered by log
          time).
        </Typography>
      )}

      <DataTable
        columns={columns}
        data={rows}
        loading={loading}
        page={loadAll ? 0 : table.page}
        pageSize={loadAll ? rows.length || 1 : table.pageSize}
        totalElements={totalElements}
        onPageChange={table.setPage}
        onPageSizeChange={table.setPageSize}
        sorting={loadAll ? undefined : table.sorting}
        onSortingChange={loadAll ? undefined : table.onSortingChange}
        hidePagination={loadAll}
        emptyMessage="No trend data for the selected filters"
      />
    </Box>
  );
}
