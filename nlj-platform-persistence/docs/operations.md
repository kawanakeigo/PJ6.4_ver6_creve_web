# NLJ運用

- port: 8082
- DB: nlj
- Cookie: NLJ_ADMIN_SESSION
- LINE tokenは `X-Line-ID-Token` だけで受け付け、URL・localStorage・ログへ保存しない
- `LINE_CHANNEL_ID` 未設定時に本人API・投票を成功させない
- 公開ランキングとArtist EXPは明示設定まで公開しない
