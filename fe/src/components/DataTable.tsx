import { ReactNode } from 'react';
import {
  flexRender,
  getCoreRowModel,
  useReactTable,
  type ColumnDef,
  type OnChangeFn,
  type SortingState,
} from '@tanstack/react-table';
import {
  Box,
  CircularProgress,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TableSortLabel,
  Tooltip,
  Typography,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

export interface DataTableProps<T> {
  columns: ColumnDef<T, any>[];
  data: T[];
  loading?: boolean;
  // Server-side pagination state (0-based page).
  page: number;
  pageSize: number;
  totalElements: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  // Optional server-side sorting.
  sorting?: SortingState;
  onSortingChange?: OnChangeFn<SortingState>;
  // Optional row actions — when provided, an "Actions" column is appended.
  onEdit?: (row: T) => void;
  onDelete?: (row: T) => void;
  emptyMessage?: string;
  // Extra content rendered to the right of nothing — reserved for future use.
  toolbar?: ReactNode;
}

export function DataTable<T>({
  columns,
  data,
  loading,
  page,
  pageSize,
  totalElements,
  onPageChange,
  onPageSizeChange,
  sorting,
  onSortingChange,
  onEdit,
  onDelete,
  emptyMessage = 'No records found',
}: DataTableProps<T>) {
  const hasActions = Boolean(onEdit || onDelete);

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    manualSorting: Boolean(onSortingChange),
    state: { sorting: sorting ?? [] },
    onSortingChange,
  });

  const colSpan = columns.length + (hasActions ? 1 : 0);

  return (
    <Paper variant="outlined">
      <TableContainer>
        <Table size="small">
          <TableHead>
            {table.getHeaderGroups().map((hg) => (
              <TableRow key={hg.id}>
                {hg.headers.map((header) => {
                  const canSort = Boolean(onSortingChange) && header.column.getCanSort();
                  const sortDir = header.column.getIsSorted();
                  return (
                    <TableCell
                      key={header.id}
                      sx={{ fontWeight: 600, whiteSpace: 'nowrap' }}
                      sortDirection={sortDir || false}
                    >
                      {canSort ? (
                        <TableSortLabel
                          active={Boolean(sortDir)}
                          direction={sortDir === 'desc' ? 'desc' : 'asc'}
                          onClick={header.column.getToggleSortingHandler()}
                        >
                          {flexRender(header.column.columnDef.header, header.getContext())}
                        </TableSortLabel>
                      ) : (
                        flexRender(header.column.columnDef.header, header.getContext())
                      )}
                    </TableCell>
                  );
                })}
                {hasActions && (
                  <TableCell align="right" sx={{ fontWeight: 600 }}>
                    Actions
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={colSpan} align="center" sx={{ py: 6 }}>
                  <CircularProgress size={28} />
                </TableCell>
              </TableRow>
            ) : data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={colSpan} align="center" sx={{ py: 6 }}>
                  <Typography color="text.secondary">{emptyMessage}</Typography>
                </TableCell>
              </TableRow>
            ) : (
              table.getRowModel().rows.map((row) => (
                <TableRow key={row.id} hover>
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id}>
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
                  {hasActions && (
                    <TableCell align="right">
                      <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                        {onEdit && (
                          <Tooltip title="Edit">
                            <IconButton size="small" color="primary" onClick={() => onEdit(row.original)}>
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                        {onDelete && (
                          <Tooltip title="Delete">
                            <IconButton size="small" color="error" onClick={() => onDelete(row.original)}>
                              <DeleteIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Stack>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <Box sx={{ borderTop: 1, borderColor: 'divider' }}>
        <TablePagination
          component="div"
          count={totalElements}
          page={page}
          onPageChange={(_e, newPage) => onPageChange(newPage)}
          rowsPerPage={pageSize}
          onRowsPerPageChange={(e) => {
            onPageSizeChange(parseInt(e.target.value, 10));
            onPageChange(0);
          }}
          rowsPerPageOptions={[5, 10, 20, 50]}
        />
      </Box>
    </Paper>
  );
}
