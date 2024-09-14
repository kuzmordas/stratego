import Image from "next/image";
import {
  Event,
  BothDie,
  Die,
  DisactivateMine,
  Drowning,
  Kill,
  KillMarshal,
  CaptureFlag,
  isBothDieEvent,
  isDieEvent,
  isDisactivateMineEvent,
  isDrowningEvent,
  isKillEvent,
  isKillMarshalEvent,
  isCaptureFlag
} from "@/domain/game";
import { Placeholder } from "@telegram-apps/telegram-ui";

const DrowningEventView = (props: {event: Drowning}) => {
  const { event } = props;
  return (
    <Placeholder
      description={`Unit '${event.unit.type}' is drowing`}
      header="Drowning"
    >
      <Image
        width={144}
        height={144}
        alt="unit"
        src={`/${event.unit.player}/${event.unit.type}.png`}
      />
    </Placeholder>
  )
}

const KillEventView = (props: {event: Kill}) => {
  const { event } = props;
  return (
    <Placeholder
      description={`Unit '${event.killed.type}' was killed`}
      header="Kill"
    >
      <Image
        width={144}
        height={144}
        alt="unit"
        src={`/${event.killed.player}/${event.killed.type}.png`}
      />
    </Placeholder>
  )
}

const DieEventView = (props: {event: Die}) => {
  const { event } = props;
  return (
    <Placeholder
      description={`Unit '${event.killed.type}' was die during attack`}
      header="Kill"
    >
      <Image
        width={144}
        height={144}
        alt="unit"
        src={`/${event.killed.player}/${event.killed.type}.png`}
      />
    </Placeholder>
  )
}

const BothDieView = (props: {event: BothDie}) => {
  const { event } = props;
  return (
    <Placeholder
      description={`Both units was die`}
      header="Kill"
    >
      <Image
        alt="unit"
        src={`/${event.attacking.player}/${event.attacking.type}.png`}  
        width={144}
        height={144}
      />
      <Image
        alt="unit"
        src={`/${event.defending.player}/${event.defending.type}.png`}
        width={144}
        height={144}
      />
    </Placeholder>
  )
}

const DisactivateMineView = (props: {event: DisactivateMine}) => {
  const { event } = props;
  return (
    <Placeholder
      description={`Mine was disactivated`}
      header="Kill"
    >
      <Image
        alt="unit"
        src={`/${event.mine.player}/${event.mine.type}.png`}
        width={144}
        height={144}
      />
    </Placeholder>
  )
}

const KillMarshalView = (props: {event: KillMarshal}) => {
  const { event } = props;
  return (
    <Placeholder
      description={`Marshal was killed`}
      header="Kill"
    >
      <Image
        alt="unit"
        src={`/${event.marshal.player}/${event.marshal.type}.png`}
        width={144}
        height={144}
      />
    </Placeholder>
  )
}

const CaptureFlagView = (props: {event: CaptureFlag}) => {
  const { event } = props;
  return (
    <Placeholder
      description={`${event.flag.player.toUpperCase()} lose flag`}
      header="Capture flag"
    >
      <Image
        alt="unit"
        src={`/${event.flag.player}/${event.flag.type}.png`}
        width={144}
        height={144}
      />
    </Placeholder>
  )
}

type EventProps = {
  event: Event,
  onCloseEvent: () => void
}

export const EventView = (props: EventProps) => {

  if (isDrowningEvent(props.event)) return <DrowningEventView event={props.event}/>
  if (isKillEvent(props.event)) return <KillEventView event={props.event}/>
  if (isBothDieEvent(props.event)) return <BothDieView event={props.event}/>
  if (isDieEvent(props.event)) return <DieEventView event={props.event}/>
  if (isDisactivateMineEvent(props.event)) return <DisactivateMineView event={props.event}/>
  if (isKillMarshalEvent(props.event)) return <KillMarshalView event={props.event}/>
  if (isCaptureFlag(props.event)) return <CaptureFlagView event={props.event}/>

  return <span>{"unexpected event"}</span>
}