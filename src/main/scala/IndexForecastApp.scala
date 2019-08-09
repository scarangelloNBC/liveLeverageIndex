object IndexForecastApp extends App {
 val date = Utils.yearMonthAndDay
 val links = Utils.todaysGamesLinks(date)
 for(elem <- links) {
  Utils.getGameInfo(elem)
 }
}
