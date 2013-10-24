<jsp:useBean id="url" class="java.lang.String" scope="request"/>
<jsp:useBean id="rurl" class="java.lang.String" scope="request"/>
<jsp:useBean id="name" class="java.lang.String" scope="request"/>

Welcome <b><%= name %>!</b>

<form method="POST" action="<%= url %>">
    <table>
        <tr>
            <td>Enter your name:</td>
            <td><input type="text" name="name" size="20" maxlength="20"/></td>
        </tr>
        <tr>
            <td align="center"><input type="submit" value="OK"/></td>
        </tr>
    </table>
</form>
