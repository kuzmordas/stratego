import Image from "next/image";
import { Text } from "@telegram-apps/telegram-ui"
import { EntityAndPosition } from "@/domain/game";
import styles from "./activeUnit.module.css";


type ActiveUnitProps = {
  entity: EntityAndPosition
}

export const ActiveUnitView = (props: ActiveUnitProps) => {

  const { entity: { entity, position } } = props;
  
  return (
    <div className={styles.activeUnit}>
      <Image
        width={48}
        height={48}
        src={`/${entity.player}/${entity.type}.png`}
        alt="unit"
      />
      <div className={styles.activeUnit__data}>
        <Text weight="3">{entity.type}</Text>
        <Text weight="3">{`x: ${position.x} y: ${position.y}`}</Text>
        <Text weight="3">{`force: ${entity.force ? entity.force : '-'}`}</Text>
      </div>
    </div>
  )
}