var client = new HttpClientBuilder()
    .supportInsecureConnections()
    .supportTLSv1()
    .get()
    .followRedirects(HttpClient.Redirect.ALWAYS)
    .cookieHandler(new StaticCookieHandler("lng=ua; adui=new.0UA49135aa4T5e3cf9f1; ltm=session; userfam=Flux+Aeon; scook=1; slmp=28645ccd1fcbbb19ee06cadc762911b8; slmx=8f964a; mml=ua; tpl=new; MMSID=060693d7d907833b29a54ab7d28ea16a; MAUTH=08cP3RkoAhFhpbQCWaz1gw0PKhXgTXpthy9bg9%2BQrhfMmkzZ8pkrVUT6ISYHRzdmPwl%2FwYTHpePw8nnwVj%2Fzj2u9X188zS9inh0xOTgX5k5Q1G05G7PQB1FBv6oVdFsYP0VaVOOGobZRUACV3V8vw8ia2i4EF3bN01HFYYJ4UIjmzjgkp%2B29Lxkjhw%3D%3D"))
    .build();
var request = HttpRequest.newBuilder()
    .uri(URI.create("https://webmail.meta.ua/read_body.php?mailbox=INBOX&passed_id=8&PG_SHOWALL=0&newsort=0&startMessage=1"))
    .GET()
    .build();
var response = client.send(request, BodyHandlers.ofString());
System.out.println(response.body());

