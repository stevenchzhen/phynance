import axios from 'axios';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { User } from '../types/User';

const API_URL = 'http://localhost:8080/api/users';

export const fetchUsers = async (): Promise<User[]> => {
  const { data } = await axios.get<User[]>(API_URL);
  return data;
};

export const useUsers = () => useQuery(['users'], fetchUsers);

export const createUser = async (user: Omit<User, 'id'>): Promise<User> => {
  const { data } = await axios.post<User>(API_URL, user);
  return data;
};

export const useCreateUser = () => {
  const queryClient = useQueryClient();
  return useMutation(createUser, {
    onSuccess: () => {
      queryClient.invalidateQueries(['users']);
    },
  });
};
