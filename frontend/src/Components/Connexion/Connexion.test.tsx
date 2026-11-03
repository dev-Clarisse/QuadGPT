import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Connexion from './Connexion'

const fetchMock = vi.fn()

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status })
}

function textResponse(body: string, status: number) {
  return new Response(body, { status })
}

describe('Connexion', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  it('logs in and passes the token to the parent', async () => {
    fetchMock.mockResolvedValueOnce(jsonResponse({ token: 'jwt-token' }))
    const onLogin = vi.fn()
    render(<Connexion onLogin={onLogin} />)

    await userEvent.type(screen.getByLabelText('Email address'), 'rh@quadria.fr')
    await userEvent.type(screen.getByLabelText('Password'), 'motdepasse')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ email: 'rh@quadria.fr', password: 'motdepasse' }),
    }))
    expect(onLogin).toHaveBeenCalledWith('rh@quadria.fr', 'jwt-token')
  })

  it('shows the server message when the login fails', async () => {
    fetchMock.mockResolvedValueOnce(textResponse('Invalid email or password', 401))
    const onLogin = vi.fn()
    render(<Connexion onLogin={onLogin} />)

    await userEvent.type(screen.getByLabelText('Email address'), 'rh@quadria.fr')
    await userEvent.type(screen.getByLabelText('Password'), 'mauvais')
    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByText('Invalid email or password')).toBeInTheDocument()
    expect(onLogin).not.toHaveBeenCalled()
  })

  it('does not call the server when the passwords do not match', async () => {
    render(<Connexion onLogin={vi.fn()} />)
    await userEvent.click(screen.getByRole('button', { name: 'Create one' }))

    await userEvent.type(screen.getByLabelText('Email address'), 'rh@quadria.fr')
    await userEvent.selectOptions(screen.getByLabelText('Department / Team'), 'RH')
    await userEvent.type(screen.getByLabelText('Password'), 'motdepasse')
    await userEvent.type(screen.getByLabelText('Confirm password'), 'autre-mot')
    await userEvent.click(screen.getByRole('button', { name: 'Create account' }))

    expect(screen.getByText('Passwords do not match.')).toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('registers with the chosen department then logs in', async () => {
    fetchMock
      .mockResolvedValueOnce(new Response(null, { status: 200 }))
      .mockResolvedValueOnce(jsonResponse({ token: 'jwt-token' }))
    const onLogin = vi.fn()
    render(<Connexion onLogin={onLogin} />)
    await userEvent.click(screen.getByRole('button', { name: 'Create one' }))

    await userEvent.type(screen.getByLabelText('Email address'), 'rh@quadria.fr')
    await userEvent.selectOptions(screen.getByLabelText('Department / Team'), 'RH')
    await userEvent.type(screen.getByLabelText('Password'), 'motdepasse')
    await userEvent.type(screen.getByLabelText('Confirm password'), 'motdepasse')
    await userEvent.click(screen.getByRole('button', { name: 'Create account' }))

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/auth/register', expect.objectContaining({
      body: JSON.stringify({ email: 'rh@quadria.fr', password: 'motdepasse', department: 'RH' }),
    }))
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/auth/login', expect.anything())
    expect(onLogin).toHaveBeenCalledWith('rh@quadria.fr', 'jwt-token')
  })
})
