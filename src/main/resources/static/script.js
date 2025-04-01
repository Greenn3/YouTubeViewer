function updateDivText(){
    let select = document.getElementById("category");
    let selectedText = select.options[select.selectedIndex].text;
    let selectedValue = select.options[select.selectedIndex].value;


    document.getElementById("temp-content").innerText = "You selected: " + selectedText + ", " + selectedValue;
    window.location.href = "/watch?categoryId=" + selectedValue;

}