<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <title>OpenTok Cloud API Sample</title>
</head>
<body>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
    <script src="js/events.js"></script>
    <h2>OpenTok Cloud API Sample</h2>

    <ul>
    <#list events as event>
        <li>
            ${ event.event }
            <#if event.event == "ConnectionCreated" >
            <span data-connectionId="${ event.connection.id }" data-sessionId="${ event.sessionId }">
                <a href="/disconnect">Disconnect</a>
                <a href="/signal">Signal</a>
            </span>
            </#if>
        </li>
    </#list>
    </ul>
</body>
</html>
