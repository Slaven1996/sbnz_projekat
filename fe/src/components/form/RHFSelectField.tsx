import { Controller, type Control, type FieldPath, type FieldValues } from 'react-hook-form';
import { MenuItem, TextField } from '@mui/material';

export interface SelectOption {
  value: string | number;
  label: string;
}

type Props<T extends FieldValues> = {
  name: FieldPath<T>;
  control: Control<T>;
  label: string;
  options: SelectOption[];
  numeric?: boolean;
  allowEmpty?: boolean;
  emptyLabel?: string;
};

export function RHFSelectField<T extends FieldValues>({
  name,
  control,
  label,
  options,
  numeric,
  allowEmpty,
  emptyLabel = '— none —',
}: Props<T>) {
  return (
    <Controller
      name={name}
      control={control}
      render={({ field, fieldState }) => (
        <TextField
          select
          label={label}
          value={field.value ?? ''}
          onChange={(e) => {
            const raw = e.target.value;
            if (raw === '') field.onChange(allowEmpty ? null : undefined);
            else field.onChange(numeric ? Number(raw) : raw);
          }}
          onBlur={field.onBlur}
          error={Boolean(fieldState.error)}
          helperText={fieldState.error?.message}
          fullWidth
          size="small"
        >
          {allowEmpty && (
            <MenuItem value="">
              <em>{emptyLabel}</em>
            </MenuItem>
          )}
          {options.map((opt) => (
            <MenuItem key={opt.value} value={opt.value}>
              {opt.label}
            </MenuItem>
          ))}
        </TextField>
      )}
    />
  );
}
