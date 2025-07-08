import React, { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { Box, Button, TextField, Typography, Alert, CircularProgress } from '@mui/material';
import { useAuthContext } from '../../context/AuthContext';

interface LoginFormInputs {
  username: string;
  password: string;
}

const schema = yup.object({
  username: yup.string().email('Enter a valid email').required('Email is required'),
  password: yup
    .string()
    .min(6, 'Password must be at least 6 characters')
    .required('Password is required'),
});

const LoginForm: React.FC = () => {
  const { login, loading, error, isAuthenticated } = useAuthContext();
  const navigate = useNavigate();
  const { control, handleSubmit } = useForm<LoginFormInputs>({
    resolver: yupResolver(schema),
    defaultValues: { username: '', password: '' },
  });

  // Navigate to dashboard after successful login
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard');
    }
  }, [isAuthenticated, navigate]);

  const onSubmit = async (data: LoginFormInputs) => {
    await login(data.username, data.password);
  };

  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{ maxWidth: 400, mx: 'auto', mt: 8 }}
    >
      <Typography variant="h5" mb={2}>
        Login
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      <Controller
        name="username"
        control={control}
        render={({ field }) => (
          <TextField {...field} label="Email" fullWidth margin="normal" autoComplete="email" />
        )}
      />
      <Controller
        name="password"
        control={control}
        render={({ field }) => (
          <TextField
            {...field}
            label="Password"
            type="password"
            fullWidth
            margin="normal"
            autoComplete="current-password"
          />
        )}
      />
      <Button
        type="submit"
        variant="contained"
        color="primary"
        fullWidth
        disabled={loading}
        startIcon={loading ? <CircularProgress size={20} /> : null}
        sx={{ mt: 2 }}
      >
        Login
      </Button>
    </Box>
  );
};

export default LoginForm;
