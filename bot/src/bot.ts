import axios from 'axios';
import * as dotenv from "dotenv";
import { Bot, InlineKeyboard, InlineQueryResultBuilder } from "grammy";

dotenv.config();

const config = {
  token: process.env.TOKEN,
  backendUrl: process.env.BACKEND_URL
}

if (!config.token) throw new Error('Token for bot not provided')
if (!config.backendUrl) throw new Error('Backend url not provided')

const bot = new Bot(config.token);

bot.command("start", (ctx) => {

  const text = "*Want to play stratego with any contact from Telegram?*\nClick the button below\nYou can also send the invitation to a group or channel. In that case, the first person to click the 'Join' button will be your opponent."

  ctx.reply(
    text,
    {
      parse_mode: "Markdown",
      reply_markup: {
        inline_keyboard: [
          [
            {
              text: "Play",
              switch_inline_query_chosen_chat: {
                query: "",
                allow_bot_chats: false,
                allow_channel_chats: true,
                allow_group_chats: true,
                allow_user_chats: true,
              },
            },
          ],
        ],
      }
    }
  )
});


bot.on("inline_query", async (ctx) => {

  const user = ctx.from

  const green = InlineQueryResultBuilder
      .article("green", "Green player", {
        reply_markup: new InlineKeyboard().add({
          text: "Waiting",
          callback_data: "creating-session"
        })
      })
      .text(
        `<b>${user.username}</b> wants to play with you.\nYou will play for the <b>green</b> and your friend for the <b>red</b>`,
        { parse_mode: "HTML" },
      );

    const red = InlineQueryResultBuilder
      .article("red", "Red player", {
        reply_markup: new InlineKeyboard().add({
          text: "Waiting",
          callback_data: "creating-session"
        })
      })
      .text(
        `<b>${user.username}</b> wants to play with you.\nYou will play for the <b>red</b> and your friend for the <b>green</b>`,
        { parse_mode: "HTML" },
      );

    await ctx.answerInlineQuery(
      [green, red],
      { cache_time: 0 },
    );
});

bot.on("chosen_inline_result", async (ctx) => {
  const tgUser = ctx.update.chosen_inline_result.from;
  const player = ctx.chosenInlineResult.result_id;
  
  const user = {
    id: tgUser.id,
    fio: `${tgUser.first_name} ${tgUser.last_name}`.trim(),
    userName: tgUser.username,
    player
  }

  const res = await axios.post(`${config.backendUrl}/create-session`, {user});
  const sessionId = res.data.id;
  console.log(`Session has been created | sessiondId: ${sessionId}`);
  
  await ctx.editMessageText(`User *${user.userName}* wants to play with. Please join`, {
    parse_mode: 'Markdown',
    reply_markup: {
      inline_keyboard: [[{
        text: "Join",
        url: `https://t.me/strategogame_bot/game?startapp=${sessionId}`
      }]]
    }
  })
});

bot.start({
  onStart: (botInfo) => console.log(`Bot has been started | botId: ${botInfo.id}`)
});
