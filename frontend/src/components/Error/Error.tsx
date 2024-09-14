import { Text, Title } from "@telegram-apps/telegram-ui"
import { ApiError } from "@/infrastructure/client";
import styles from "./error.module.css";

type ErrorProps = {
  error: ApiError
}

export const ErrorView = (props: ErrorProps) => {

  const { error } = props;

  return (
    <div className={styles.error}>
      <div className={styles.errorContent}>
        <Title weight="1">ERROR</Title>
        <Text weight="3">{error.message}</Text>
        <Text weight="3">{`Error code: ${error.code}`}</Text>
        <Text weight="3">{`Error id: ${error.id}`}</Text>
      </div>
    </div>
  )
}