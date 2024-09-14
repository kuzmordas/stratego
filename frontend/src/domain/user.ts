import { type User as TgUser } from '@telegram-apps/sdk-react';

export type Player = 'green' | 'red';

export type UserWithoutPlayer = {
  id: number,
  fio: string
  userName?: string
}

export const fromTgUser = (tgUser: TgUser): UserWithoutPlayer => ({
  id: tgUser.id,
  userName: tgUser.username,
  fio: `${tgUser.firstName} ${tgUser.lastName}`.trim(),
})

export type User = {
  id: number,
  fio: string
  userName?: string,
  player: Player
}

export type InviteCreated = {
  inviteId: string,
  createdAt: String,
  status: string,
  firstUser: User
}