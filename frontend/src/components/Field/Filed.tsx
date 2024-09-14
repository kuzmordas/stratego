import Image from "next/image";
import { UnitView } from "./components/Unit/Unit";
import { CellView } from "./components/Cell/Cell";
import { EntityAndPosition, Point, ActiveSession } from "@/domain/game";

import styles from "./field.module.css";

const range = (n: number) => Array.from(Array(n).keys())

type FieldProps = {
  width: number,
  height: number,
  fieldImage: string,
  session: ActiveSession,
  activeUnit: EntityAndPosition | null
  movements: Point[],
  onCellClick: (point: Point) => void
  onUnitClick: (clicked: EntityAndPosition) => void
}  

export const FieldView = (props: FieldProps) => {

  const {
    width,
    height,
    fieldImage,
    session,
    movements,
    activeUnit,
    onCellClick,
    onUnitClick
  } = props;

  const xCoords = range(width);
  const yCoords = range(height);

  return (
    <div className={styles.fieldWrapper}>
      <Image
        className={styles.fieldImage}
        src={fieldImage}
        alt="game field"
        fill
      />
      <div className={styles.field}>
        {yCoords.map((_, y) => 
          <div key={`fielf-row-${y}`} className={styles.fieldRow}>
            {xCoords.map((_, x) => 
              <CellView
                key={`field-cell-${x}-${y}`}
                canMoveTo={ !!movements.find(p => p.x === x && p.y === y) }
                point={{x, y}}
                onClick={onCellClick}/>)}
          </div>)}
        {session.state.field.entities.map(unit => 
          <UnitView
            key={`unit-${unit.entity.id}`}
            unit={unit}
            selected={activeUnit?.entity.id === unit.entity.id}
            onClick={onUnitClick}/>)}  
      </div>
    </div>
  );
}