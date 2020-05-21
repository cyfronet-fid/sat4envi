function simplicity(){
  setTimeout(function(){

    let sidebar = document.querySelectorAll(".sidebar").item(0);

    function overlap() {
      let products = document.querySelectorAll(".products-list");
      let prodItem = products.item(0);
      let productBottomSide = prodItem.offsetTop + prodItem.clientHeight;

      let layers = document.querySelectorAll(".layers");
      let layersTopSide = layers.item(0).offsetTop;

      if(productBottomSide > layersTopSide) {
        let differenceHeight = productBottomSide - (productBottomSide - layersTopSide);

        if(differenceHeight > 0) {
        sidebar.style.height=( differenceHeight + "px");
        }
      } else {
        resetHeight();
      }
    }

    function resetHeight() {
      sidebar.style.height=("100%");
    }

    window.addEventListener("resize", overlap);

    let tabs = document.querySelector(".switch");
    if(tabs) {
      tabs.addEventListener("click", resetHeight);
    }

}, 3000);
}
