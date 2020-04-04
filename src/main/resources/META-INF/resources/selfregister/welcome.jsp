<!doctype html>
<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">

    <title>${messages['welcome.title']}</title>
</head>
<body>
<div class="jumbotron">
    <h1 class="display-4">${messages['welcome.head']}</h1>
    <p class="lead">${messages['welcome.lead']}</p>
    <hr class="my-4">
    <p>${messages['welcome.info']}</p>
    <p class="lead">
    <form method="post">
        <div class="form-group">
            <label for="email">${messages['welcome.field.email.label']}</label>
            <input type="email" class="form-control" id="email" name="email" aria-describedby="emailHelp" placeholder="${messages['welcome.field.email.placeholder']}">
            <small id="emailHelp" class="form-text text-muted">${messages['welcome.field.email.help']}</small>
        </div>
        <div class="form-check">
            <input type="checkbox" class="form-check-input" id="checkAgree" onclick="document.getElementById('submitbutton').disabled = !this.checked">
            <label class="form-check-label" for="checkAgree">${messages['welcome.field.agree.label']}</label>
        </div>
        <button id="submitbutton" disabled="disabled" class="btn btn-primary btn-lg" role="button" type="submit" class="btn btn-primary">${messages['welcome.field.submit.label']}</button>
    </form>
    </p>
</div>

<!-- Optional JavaScript -->
<!-- jQuery first, then Popper.js, then Bootstrap JS -->
<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
</body>
</html>