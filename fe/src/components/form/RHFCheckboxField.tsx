import { Controller, type Control, type FieldPath, type FieldValues } from 'react-hook-form';
import { Checkbox, FormControlLabel } from '@mui/material';

type Props<T extends FieldValues> = {
  name: FieldPath<T>;
  control: Control<T>;
  label: string;
};

export function RHFCheckboxField<T extends FieldValues>({ name, control, label }: Props<T>) {
  return (
    <Controller
      name={name}
      control={control}
      render={({ field }) => (
        <FormControlLabel
          control={
            <Checkbox
              checked={Boolean(field.value)}
              onChange={(e) => field.onChange(e.target.checked)}
              onBlur={field.onBlur}
            />
          }
          label={label}
        />
      )}
    />
  );
}
