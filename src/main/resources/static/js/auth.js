
if (sessionStorage.getItem("isLoggedIn") !== "true") {
    window.location.replace("/index.html");
}

function logout() {
    sessionStorage.removeItem("isLoggedIn");
    window.location.replace("/index.html");
}
