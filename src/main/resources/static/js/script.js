//const toggleSidebar = () => {
//    if ($(".sidebar").is(":visible")) {
//        $(".sidebar").css("display", "none");
//        $(".content").css("margin-left", "0%");
//    } else {
//        $(".sidebar").css("display", "block");
//        $(".content").css("margin-left", "20%");
//    }
//};

// sidebar visible
function toggleSidebar() {
    
    const sidebar =  document.getElementsByClassName("sidebar")[0];
    const content =  document.getElementsByClassName("content")[0];

    if(window.getComputedStyle(sidebar).display === "none"){
        sidebar.style.display = "block";
        content.style.marginLeft = "20%";
    }
    else{
        sidebar.style.display = "none";
        content.style.marginLeft = "0%";
    }
}

// session alert messages
window.onload = function() {
    setTimeout(function() {
        var messageDiv = document.getElementById("sessionMessage");
        if (messageDiv) {
            messageDiv.style.display = "none";
        }
    }, 2000); 
}

// search box funtion
$(document).ready(() => {
    const search = () => {
        let query = $("#search-input").val(); // Use # for ID selector
        console.log(query);
        
        if (query === "") {
            $(".search-result").hide();
        } else {
            //console.log(query);
            
            let url = `http://localhost:8080/search/${query}`;
            
            fetch(url)
            .then((response) => {
				return response.json();
			}).then((data) => {
				//data
				console.log(data);
				
				let text= `<div class='list-group'>`
				
				data.forEach((contact) => {
					text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action' style='color: black;'> ${contact.name} </a>`
				});
				
				text+=`</div>`;
				
				$(".search-result").html(text);
				$(".search-result").show();
				
			});
            
            //$(".search-result").show();
        }
    };

    // Bind the search function to the keyup event of the input field
    $("#search-input").on("keyup", search);
});
