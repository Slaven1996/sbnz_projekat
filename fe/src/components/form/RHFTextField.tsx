import { Controller, type Control, type FieldPath, type FieldValues } from 'react-hook-form';
import { TextField, type TextFieldProps } from '@mui/material';

type Props<T extends FieldValues> = {
  name: FieldPath<T>;
  control: Control<T>;
  label: string;
  type?: 'text' | 'number' | 'email' | 'password';
} & Omit<TextFieldProps, 'name' | 'type' | 'error' | 'helperText' | 'value' | 'onChange'>;

export function RHFTextField<T extends FieldValues>({
  name,
  control,
  label,
  type = 'text',
  ...rest
}: Props<T>) {
  return (
    <Controller
      name={name}
      control={control}
      render={({ field, fieldState }) => (
        <TextField
          {...rest}
          label={label}
          type={type}
          value={field.value ?? ''}
          onChange={(e) => {
            if (type === 'number') {
              const v = e.target.value;
              field.onChange(v === '' ? undefined : Number(v));
            } else {
              field.onChange(e.target.value);
            }
          }}
          onBlur={field.onBlur}
          inputRef={field.ref}
          error={Boolean(fieldState.error)}
          helperText={fieldState.error?.message}
          fullWidth
          size="small"
        />
      )}
    />
  );
}
