import { Avatar, Text } from "@telegram-apps/telegram-ui"
import { User } from "@/domain/user"
import styles from "./userInfo.module.css";

type UserInfoProps = {
  user: User,
  active?: boolean
}

export const UserInfoView = (props: UserInfoProps) => {

  const { user } = props;
  const acronym = user.fio.split(' ').map(s => s[0]).join('')

  return (
    <div className={styles.userInfo}>
      <Avatar
        acronym={acronym}
        size={48}
      />
      <div className={styles.userInfo__data}>
        <Text weight="3">{user.userName || user.fio}</Text>
      </div>
    </div>
  )
}