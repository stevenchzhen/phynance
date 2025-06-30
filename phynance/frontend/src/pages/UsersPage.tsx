import React from "react";
import { useUsers, useCreateUser } from "../api/users";
import { useForm } from "react-hook-form";
import { User } from "../types/User";
import {
  Box,
  Button,
  Container,
  TextField,
  Typography,
  List,
  ListItem,
  ListItemText,
} from "@mui/material";

interface UserFormInput {
  username: string;
  email: string;
}

const UsersPage: React.FC = () => {
  const { data: users, isLoading } = useUsers();
  const createUser = useCreateUser();
  const { register, handleSubmit, reset } = useForm<UserFormInput>();

  const onSubmit = (data: UserFormInput) => {
    createUser.mutate(data, {
      onSuccess: () => reset(),
    });
  };

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" gutterBottom>
        Users
      </Typography>
      <Box component="form" onSubmit={handleSubmit(onSubmit)} mb={4}>
        <TextField
          label="Username"
          fullWidth
          margin="normal"
          {...register("username", { required: true })}
        />
        <TextField
          label="Email"
          fullWidth
          margin="normal"
          {...register("email", { required: true })}
        />
        <Button
          type="submit"
          variant="contained"
          color="primary"
          disabled={createUser.isLoading}
        >
          Add User
        </Button>
      </Box>
      <Typography variant="h6">User List</Typography>
      {isLoading ? (
        <Typography>Loading...</Typography>
      ) : (
        <List>
          {users?.map((user: User) => (
            <ListItem key={user.id}>
              <ListItemText primary={user.username} secondary={user.email} />
            </ListItem>
          ))}
        </List>
      )}
    </Container>
  );
};

export default UsersPage;
