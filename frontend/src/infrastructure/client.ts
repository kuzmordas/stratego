import { Entity, Point, ActiveSession, InactiveSession } from "@/domain/game";
import { Player, UserWithoutPlayer } from "@/domain/user";

export class ApiError extends Error {
  statusCode: number;
  code: number | null;
  details: string | null;
  id: string;

  constructor(message: string, statusCode: number, id: string, details: string | null = null, code: number | null = null) {
    super(message);

    this.id = id;
    this.statusCode = statusCode;
    this.code = code;
    this.details = details;
  }
}

export const debug = (data: any) =>
  fetch('/api/debug', {
    method: 'POST',
    body: JSON.stringify(data)
  })

export const sendCommand = async <T,>(command: string, data: any) => {

  const response = await fetch('/api', {
    method: 'POST',
    body: JSON.stringify({command, data})
  })

  if (response.status === 200) {
    return response.json() as T
  } else {
    const err = await response.json();

    throw new ApiError(err.message, response.status, err.id, err.details || null, err.code || null);
  }
}

type GetSessionByIdData = {
  sessionId: string,
  userId: number
}

export const getSessionById = (data: GetSessionByIdData) => 
  sendCommand<ActiveSession | null>('get-session-by-id', data)

type MoveData = {
  sessionId: string,
  userId: number,
  move: {
    player: Player,
    entity: Entity,
    to: Point
  }
}

export const move = (data: MoveData) => sendCommand<ActiveSession>('move', data);

type GetMovementsData = {
  sessionId: string,
  entity: Entity
}

export const getMovements = (data: GetMovementsData) =>
  sendCommand<Point[]>('get-movements', data);

type JoinToSessionData = {
  sessionId: string,
  user: UserWithoutPlayer
}

export const joinToSession = (data: JoinToSessionData) => 
  sendCommand<ActiveSession | InactiveSession | null>('join-to-session', data);
