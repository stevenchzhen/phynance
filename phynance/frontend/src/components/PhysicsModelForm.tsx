import React from "react";
import { useForm, Controller, SubmitHandler } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import {
  Box,
  Button,
  TextField,
  Typography,
  MenuItem,
  Slider,
  Grid,
  Alert,
  CircularProgress,
} from "@mui/material";
import Autocomplete from "@mui/material/Autocomplete";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { useStockSearch } from "../hooks/useStockSearch";
import dayjs from "dayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";

export type ModelType = "harmonic" | "thermodynamic" | "wave";

export interface PhysicsModelFormData {
  symbol: string;
  startDate: Date;
  endDate: Date;
  dampingFactor: number;
  frequency: number;
  modelType: ModelType;
}

const schema = yup.object({
  symbol: yup.string().required("Stock symbol is required"),
  startDate: yup.date().required("Start date is required"),
  endDate: yup
    .date()
    .min(yup.ref("startDate"), "End date must be after start date")
    .required("End date is required"),
  dampingFactor: yup
    .number()
    .min(0)
    .max(1)
    .required("Damping factor is required"),
  frequency: yup.number().min(0.1).max(10).required("Frequency is required"),
  modelType: yup
    .string()
    .oneOf(["harmonic", "thermodynamic", "wave"])
    .required("Model type is required"),
});

const MODEL_TYPES = [
  { value: "harmonic", label: "Harmonic Oscillator" },
  { value: "thermodynamic", label: "Thermodynamic" },
  { value: "wave", label: "Wave Analysis" },
];

interface PhysicsModelFormProps {
  onSubmit: (data: PhysicsModelFormData) => void | Promise<void>;
  loading?: boolean;
  error?: string | null;
}

const PhysicsModelForm: React.FC<PhysicsModelFormProps> = ({
  onSubmit,
  loading,
  error,
}) => {
  const {
    control,
    handleSubmit,
    formState: { errors },
    watch,
    setValue,
  } = useForm<PhysicsModelFormData>({
    resolver: yupResolver(schema),
    defaultValues: {
      symbol: "",
      startDate: new Date(dayjs().subtract(1, "month").toISOString()),
      endDate: new Date(),
      dampingFactor: 0.5,
      frequency: 1,
      modelType: "harmonic",
    },
    mode: "onChange",
  });
  const symbol = watch("symbol");
  const { data: stockOptions, isLoading: searching } = useStockSearch(symbol);

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Box
        component="form"
        onSubmit={handleSubmit(onSubmit)}
        sx={{ p: 2, maxWidth: 500, mx: "auto" }}
      >
        <Typography variant="h6" mb={2}>
          Physics Model Analysis
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Controller
              name="symbol"
              control={control}
              render={({ field }) => (
                <Autocomplete
                  freeSolo
                  options={stockOptions || []}
                  getOptionLabel={(option) =>
                    typeof option === "string"
                      ? option
                      : `${option.symbol} - ${option.name}`
                  }
                  loading={searching}
                  onInputChange={(_, value) => setValue("symbol", value)}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Stock Symbol"
                      error={!!errors.symbol}
                      helperText={errors.symbol?.message}
                    />
                  )}
                />
              )}
            />
          </Grid>
          <Grid item xs={6}>
            <Controller
              name="startDate"
              control={control}
              render={({ field }) => (
                <DatePicker
                  label="Start Date"
                  value={field.value ? dayjs(field.value) : null}
                  onChange={(date) =>
                    field.onChange(date ? date.toDate() : null)
                  }
                  slotProps={{
                    textField: {
                      error: !!errors.startDate,
                      helperText: errors.startDate?.message,
                    },
                  }}
                />
              )}
            />
          </Grid>
          <Grid item xs={6}>
            <Controller
              name="endDate"
              control={control}
              render={({ field }) => (
                <DatePicker
                  label="End Date"
                  value={field.value ? dayjs(field.value) : null}
                  onChange={(date) =>
                    field.onChange(date ? date.toDate() : null)
                  }
                  slotProps={{
                    textField: {
                      error: !!errors.endDate,
                      helperText: errors.endDate?.message,
                    },
                  }}
                />
              )}
            />
          </Grid>
          <Grid item xs={12}>
            <Controller
              name="dampingFactor"
              control={control}
              render={({ field }) => (
                <Box>
                  <Typography gutterBottom>Damping Factor</Typography>
                  <Slider
                    {...field}
                    min={0}
                    max={1}
                    step={0.01}
                    valueLabelDisplay="auto"
                  />
                  {errors.dampingFactor && (
                    <Typography color="error" variant="caption">
                      {errors.dampingFactor.message}
                    </Typography>
                  )}
                </Box>
              )}
            />
          </Grid>
          <Grid item xs={12}>
            <Controller
              name="frequency"
              control={control}
              render={({ field }) => (
                <Box>
                  <Typography gutterBottom>Frequency</Typography>
                  <Slider
                    {...field}
                    min={0.1}
                    max={10}
                    step={0.1}
                    valueLabelDisplay="auto"
                  />
                  {errors.frequency && (
                    <Typography color="error" variant="caption">
                      {errors.frequency.message}
                    </Typography>
                  )}
                </Box>
              )}
            />
          </Grid>
          <Grid item xs={12}>
            <Controller
              name="modelType"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  select
                  label="Model Type"
                  fullWidth
                  error={!!errors.modelType}
                  helperText={errors.modelType?.message}
                >
                  {MODEL_TYPES.map((type) => (
                    <MenuItem key={type.value} value={type.value}>
                      {type.label}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
          </Grid>
          <Grid item xs={12}>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              fullWidth
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} /> : null}
            >
              Run Analysis
            </Button>
          </Grid>
        </Grid>
      </Box>
    </LocalizationProvider>
  );
};

export default PhysicsModelForm;
