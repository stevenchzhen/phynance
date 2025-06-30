import React from 'react';
import { render, screen, fireEvent, waitFor } from '../../test-utils';
import PhysicsModelForm, { PhysicsModelFormData } from '../PhysicsModelForm';

describe('PhysicsModelForm', () => {
  it('shows validation errors', async () => {
    render(<PhysicsModelForm onSubmit={jest.fn()} />);
    fireEvent.click(screen.getByRole('button', { name: /run analysis/i }));
    expect(await screen.findAllByText(/required/i)).toHaveLength(3);
  });

  it('submits valid data', async () => {
    const handleSubmit = jest.fn();
    render(<PhysicsModelForm onSubmit={handleSubmit} />);
    fireEvent.change(screen.getByLabelText(/stock symbol/i), { target: { value: 'AAPL' } });
    fireEvent.change(screen.getByLabelText(/start date/i), { target: { value: '2024-01-01' } });
    fireEvent.change(screen.getByLabelText(/end date/i), { target: { value: '2024-01-02' } });
    fireEvent.click(screen.getByRole('button', { name: /run analysis/i }));
    await waitFor(() => expect(handleSubmit).toHaveBeenCalled());
  });
});
