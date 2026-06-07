import { useMemo, useState } from 'react';
import type { SortingState } from '@tanstack/react-table';

export function useTableState(defaultSize = 10, defaultSort?: SortingState) {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(defaultSize);
  const [sorting, setSorting] = useState<SortingState>(defaultSort ?? []);

  const sortParam = useMemo(() => {
    if (!sorting.length) return undefined;
    const { id, desc } = sorting[0];
    return `${id},${desc ? 'desc' : 'asc'}`;
  }, [sorting]);

  return {
    page,
    pageSize,
    sorting,
    sortParam,
    setPage,
    setPageSize,
    onSortingChange: (updater: SortingState | ((old: SortingState) => SortingState)) => {
      setSorting(updater);
      setPage(0);
    },
  };
}
