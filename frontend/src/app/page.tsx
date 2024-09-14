'use client';

import { useEffect, useRef, useState } from "react";
import { useInitData, useThemeParams, type User as TgUser } from '@telegram-apps/sdk-react';
import { Modal, Text } from '@telegram-apps/telegram-ui';
import { EventView, FieldView, UserInfoView, LoadingView, ActiveUnitView, ErrorView } from "@/components";
import * as Client from "@/infrastructure/client";
import {
  EntityAndPosition,
  Event,
  getLastEvent,
  isMovedEvent,
  isNothingEvent,
  Point,
  ActiveSession,
  isInactiveSession,
  getRedPlayer,
  getGreenPlayer
} from "@/domain/game";
import { useInterval } from "@/hooks";

import styles from "./page.module.css";
import { fromTgUser } from "@/domain/user";

type GameProps = {
  session: ActiveSession,
  user: TgUser
}  

function Game(props: GameProps) {

  const [activeUnit, setActiveUnit] = useState<EntityAndPosition | null>(null);
  const [session, setSession] = useState(props.session);
  const [movements, setMovements] = useState<Point[]>([]);
  const [event, setEvent] = useState<Event | null>(null);
  const [error, setError] = useState<Client.ApiError | null>(null);
  const lastEventRef = useRef<Event>();

  const player = [session.firstUser, session.secondUser].find(u => u.id === props.user.id)!.player

  const updateLastEvent = (session: ActiveSession) => {
    const lastEvent = getLastEvent(session);        
    if (lastEvent && !isMovedEvent(lastEvent) && !isNothingEvent(lastEvent)) {  
      if (lastEventRef.current?.id !== (lastEvent as Event).id) {
        setEvent(lastEvent);  
        lastEventRef.current = lastEvent;
      }
    }
  }

  useInterval(() => {
    if (session.state.winner) return;

    Client.getSessionById({sessionId: props.session.id, userId: props.user.id})
      .then((session) => {
        if (session === null) return;
        updateLastEvent(session);
        setSession(session);
      })
      .catch(setError);;
  }, 1000);


  const onUnitClick = (clicked: EntityAndPosition) => {
    if (session.state.winner) return;

    // switch active unit
    if (activeUnit && activeUnit.entity.player === clicked.entity.player) {
      Client.getMovements({sessionId: session.id, entity: clicked.entity})
        .then(movements => {
          setActiveUnit(clicked);
          setMovements(movements);
        })
        .catch(setError);;

      return;
    }

    // atack another unit
    if (activeUnit && activeUnit.entity.player !== clicked.entity.player) {
      Client.move({
        sessionId: session.id,
        userId: props.user.id,
        move: {
          player,
          entity: activeUnit.entity,
          to: clicked.position
        }
      }).then(newSession => {
        updateLastEvent(newSession);        
        setSession(newSession);
        setActiveUnit(null);
        setMovements([]);
      })
      .catch(setError);;

      return;
    }

    if (clicked.entity.player !== player) return;

    if (clicked.entity.id === activeUnit?.entity.id) {
      setActiveUnit(null);
      setMovements([]);
      return;
    }

    Client.getMovements({sessionId: session.id, entity: clicked.entity})
      .then(movements => {
        setActiveUnit(clicked);
        setMovements(movements);
      })
      .catch(setError);;
  }

  const onCellClick = (point: Point) => {
    if (session.state.winner) return;

    if (activeUnit && 
      activeUnit.entity.player === session.state.currentPlayer &&
      !!movements.find(p => p.x === point.x && p.y === point.y)
    ) {
      Client.move({
        sessionId: session.id,
        userId: props.user.id,
        move: {
          player,
          entity: activeUnit.entity,
          to: point
        }
      }).then(newSession => {
        updateLastEvent(newSession);
        setSession(newSession);
        setActiveUnit(null);
        setMovements([]);
      })
      .catch(setError);
    } else {
      setActiveUnit(null);
      setMovements([]);
    }
  }

  if (error) return <ErrorView error={error}/>

  if (!event && session.state.winner) return (
    <div className={styles.main}>
      <Text weight="1" style={{margin: 'auto'}}>
        {`${session.state.winner.toUpperCase()} is winner!`}
      </Text>
    </div>
  );

  return (
    <div className={styles.main}>
      <div className={styles.main__infoWrapper}>
        <UserInfoView user={getRedPlayer(session)}/>
        {activeUnit && activeUnit.entity.player === 'red' && <ActiveUnitView entity={activeUnit}/>}
      </div> 
      <FieldView
        width={10}
        height={10}
        fieldImage="/field.png"
        session={session}
        activeUnit={activeUnit}
        movements={movements}
        onCellClick={onCellClick}
        onUnitClick={onUnitClick} 
      />
      <div className={styles.main__infoWrapper}>
        <UserInfoView user={getGreenPlayer(session)}/>
        {activeUnit && activeUnit.entity.player === 'green' && <ActiveUnitView entity={activeUnit}/>}
      </div>

      <Modal
        // header={<ModalHeader >Only iOS header</ModalHeader>}
        open={!!event}
        onOpenChange={(modal) => { if (!modal) setEvent(null); }}    
      >
        { event && <EventView event={event} onCloseEvent={() => setEvent(null)}/> }
      </Modal>
    </div>
  );
}


type GameLoadProps = {}

export default function GameLoad(_: GameLoadProps) {

  const initData = useInitData();
  const themeParams = useThemeParams();
  const sessionId = initData?.startParam;

  const [session, setSession] = useState<ActiveSession | null>(null);
  const [error, setError] = useState<Client.ApiError | null>(null)

  // useEffect(() => {
  //   Client.debug(initData?.user);
  // }, []);

  useInterval(() => {
    if (sessionId === 'debug') return;
    // const sessionId = 'eb3cdf42-542c-4f47-8461-522a5c0ae940';
    
    if (!error && initData?.user && sessionId && !session) {
      const user = fromTgUser(initData.user);

      Client.joinToSession({sessionId, user})
        .then(session => {
          if (session === null) return;
          if (isInactiveSession(session)) return;
          setSession(session);
        })
        .catch(setError);
    }
    
  }, 1000);

  return (
    <div style={{width: '100vw', height: '100vh', backgroundColor: themeParams.bgColor}}>
      { error ? 
          <ErrorView error={error}/>
          : (!initData?.user) 
            ? <Text weight="3">oops, thare are no user info...</Text>
            : !session 
              ? <LoadingView/>
              : <Game session={session} user={initData?.user}/> }
    </div>
  )
}
