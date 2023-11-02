const fs = require("fs");
//很好的转换器 这使我的网页充满mdt 爱来自nodejs
const str = fs.readFileSync("./icons.properties").toString().replaceAll("\r", "");
const arr = str.split("\n");
const all = [];
!fs.existsSync("icons") && fs.mkdirSync("icons");
arr.forEach(i => {
    let a = i.split("="), b;
    a && (b = a[1].split("|"));
    try {
        b && fs.copyFileSync("./../../assets-raw/sprites_out/ui/" + b[1] + ".png", "./icons/" + a[0] + ".png");
        b && all.push(Number(a[0]));
    } catch (_e) {
    }
    fs.writeFileSync("./icons.json", JSON.stringify(all))
});