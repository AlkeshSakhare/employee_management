
let correctCaptchaAnswer;

function generateCaptcha() {

    const firstNumber = Math.floor(Math.random() * 10) + 1;
    const secondNumber = Math.floor(Math.random() * 10) + 1;

    correctCaptchaAnswer = firstNumber + secondNumber;

    document.getElementById("captchaQuestion").textContent =
        `${firstNumber} + ${secondNumber} = ?`;

    document.getElementById("captchaAnswer").value = "";
}

document.getElementById("refreshCaptcha")
    .addEventListener("click", generateCaptcha);

document.getElementById("loginForm")
    .addEventListener("submit", function(event) {

        event.preventDefault();

        const username =
            document.getElementById("username").value.trim();

        const password =
            document.getElementById("password").value;

        const captchaAnswer =
            Number(document.getElementById("captchaAnswer").value);

        const message =
            document.getElementById("loginMessage");

        if (username !== "Admin" || password !== "Admin") {

            message.textContent = "Invalid username or password.";
            message.style.color = "red";
            generateCaptcha();
            return;
        }

        if (captchaAnswer !== correctCaptchaAnswer) {

            message.textContent = "Incorrect captcha. Try again.";
            message.style.color = "red";
            generateCaptcha();
            return;
        }

        sessionStorage.setItem("isLoggedIn", "true");

        window.location.href = "/home.html";
    });

generateCaptcha();
