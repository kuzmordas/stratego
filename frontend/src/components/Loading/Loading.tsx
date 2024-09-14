import { Spinner, Text } from "@telegram-apps/telegram-ui"
import styles from "./loading.module.css";

type LoadingViewProps = {
}

export const LoadingView = (_: LoadingViewProps) => {

  return (
    <div className={styles.loadingContainer}>
      <div className={styles.loadingContainer__content}>
        <Spinner size="l"/>
        <Text caps={true} weight="3">
          loading...
        </Text>
      </div>
    </div>
  )
}