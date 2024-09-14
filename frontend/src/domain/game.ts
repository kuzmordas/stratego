import { Player, User } from "./user"

export type Point = {
  x: number,
  y: number
}

export type Entity = {
  id: number,
  type: string,
  force?: number,
  player: 'green' | 'red'
}

export type EntityAndPosition = {
  entity: Entity,
  position: Point
}

export type GameState = {
  currentPlayer: string,
  field: {
    entities: EntityAndPosition[],
    leftLake: Point[],
    rightLake: Point[]
  },
  winner?: Player | null
}

type AbstractEvent = {
  id: string,
  type: string
}

export type Drowning = {
  unit: Entity
} & AbstractEvent

export const isDrowningEvent = (e: AbstractEvent): e is Drowning =>  e.type === 'drowning';

export type Moved = {
  unit: Entity
} & AbstractEvent

export const isMovedEvent = (e: AbstractEvent): e is Moved =>  e.type === 'moved';

export type Kill = {
  attacking: Entity
  killed: Entity
} & AbstractEvent

export const isKillEvent = (e: AbstractEvent): e is Kill =>  e.type === 'kill';

export type Die = {
  defending: Entity
  killed: Entity
} & AbstractEvent

export const isDieEvent = (e: AbstractEvent): e is Die =>  e.type === 'die';

export type BothDie = {
  attacking: Entity
  defending: Entity
} & AbstractEvent

export const isBothDieEvent = (e: AbstractEvent): e is BothDie =>  e.type === 'bothDie';

export type DisactivateMine = {
  sapper: Entity
  mine: Entity
} & AbstractEvent

export const isDisactivateMineEvent = (e: AbstractEvent): e is DisactivateMine =>  e.type === 'disactiveMine';

export type KillMarshal = {
  spy: Entity,
  marshal: Entity
} & AbstractEvent

export const isKillMarshalEvent = (e: AbstractEvent): e is KillMarshal =>  e.type === 'killMarshal';

export type CaptureFlag = {
  unit: Entity,
  flag: Entity
} & AbstractEvent

export const isCaptureFlag = (e: AbstractEvent): e is CaptureFlag =>  e.type === 'captureFlag';

export type Nothing = {} & AbstractEvent;

export const isNothingEvent = (e: AbstractEvent): e is Nothing =>  e.type === 'nothing';

export type Event = Drowning | Moved | Kill | Die | BothDie | DisactivateMine | KillMarshal | CaptureFlag | Nothing


export type InactiveSession = {
  id: string,
  firstUser: User
  state: GameState
  updatedAt: string | null;
}

export const isInactiveSession = (s: InactiveSession | ActiveSession): s is InactiveSession =>
  (s as ActiveSession).secondUser === undefined;

export type ActiveSession = {
  id: string,
  firstUser: User
  secondUser: User
  state: GameState
  events: Event[]
  createdAt: string
  updatedAt: string | null;
}

export const isActiveSession = (s: InactiveSession | ActiveSession): s is ActiveSession =>
  (s as ActiveSession).secondUser !== undefined;

export const getLastEvent = (session: ActiveSession): Event | null =>
  session.events[session.events.length - 1] || null

export const getRedPlayer = (session: ActiveSession): User =>
  session.firstUser.player === 'red' ? session.firstUser : session.secondUser

export const getGreenPlayer = (session: ActiveSession): User =>
  session.firstUser.player === 'green' ? session.firstUser : session.secondUser

