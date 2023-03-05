'use strict';

var guestId = localStorage.getItem('guestid');
if(guestId === null || guestId.length != 32) {
  guestId = generateUuid().replaceAll('-', '');
  localStorage.setItem('guestid', guestId);
  console.log('Save guestId ' + guestId);
}
const gids = document.querySelectorAll('.gid');
gids.forEach(gid => {
  gid.value = guestId;
});
const structure = document.getElementById('structure');
structure.href += '?guestId=' + guestId;
var link = window.location.href
console.log(link);
if (link == 'http://localhost:8080/' || link == 'http://pcbuilding.link/' || link == 'http://www.pcbuilding.link/'
        || link == 'https://localhost/' || link == 'https://pcbuilding.link/' || link == 'https://www.pcbuilding.link/') {
  var url = new URL(link);
  url.searchParams.append('guestId', guestId);
  location.href = url; // redirect
}
checkedTotal();

// var link = window.location.href;
// var url = new URL(link);
// if (!url.searchParams.get('guestId')) {
//   url.searchParams.append('guestId', guestId);
//   location.href = url;
//   console.log('guestId ' + guestId);
// }

// 登録（submit）した際に、ページが上に移動するのを防ぐために、何pxスクロールしたかを求めるjavascriptです。
window.onscroll = function() {
  var body = window.document.body;
  var html = window.document.documentElement;
  var scrollTop = body.scrollTop || html.scrollTop;
  const elms = document.querySelectorAll('.get_body_scroll_px');
  elms.forEach(e => {
    e.value = scrollTop; // id="get_body_scroll_px"のvalueに自動的にスクロールが何pxか表示されるよう指定しています
  });
}

window.onload = function() {
  const body = window.document.body;
  scrollTo(0, body.getAttribute('data-scroll'));
}


function generateUuid() {
  // https://github.com/GoogleChrome/chrome-platform-analytics/blob/master/src/internal/identifier.js
  // const FORMAT: string = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
  let chars = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".split("");
  for (let i = 0, len = chars.length; i < len; i++) {
      switch (chars[i]) {
          case "x":
              chars[i] = Math.floor(Math.random() * 16).toString(16);
              break;
          case "y":
              chars[i] = (Math.floor(Math.random() * 4) + 8).toString(16);
              break;
      }
  }
  return chars.join("");
}


function changeHeightHeader() {
  const contents = document.getElementById('contents');
  const header = document.getElementById('header');
  contents.style.paddingTop = header.clientHeight + 5 + 'px';
  console.log(header.clientHeight);
}
changeHeightHeader();
window.addEventListener('resize', changeHeightHeader);



function radioChecked(assemId, device) {
  const checkedItem = document.getElementById(assemId);
  const radio = checkedItem.children.item(0); // input-tag type radio
  radio.checked = true;
  
  const deviceRows = document.getElementsByClassName(device);
  hiddenRowAll(deviceRows);
  //appearRow(checkedItem.parentNode);
  checkedTotal(); 
}

function expandChange(device) {
  const deviceRows = document.getElementsByClassName(device);
  const expandNow = deviceRows[0].children.item(0).children.item(1).textContent;
  if (expandNow == 'expand_more') {
    appearRowAll(deviceRows);
  } else if (expandNow == 'expand_less') {
    hiddenRowAll(deviceRows);
  }
}

function hiddenRowAll(rows) {
  for(let i=0; i<rows.length; i++) {
    const items = rows[i];
    if (items.children.item(1).children.item(0).checked) {
      console.log('checked ' + i);
      continue;
    }
    for(let j=0; j<items.children.length; j++) {
      if (i!=0 || j!=0) {
        items.children.item(j).classList.add('zero');
      }
    }
  }
  expandChangeMore(rows);
}

// function appearRow(rows) {
//   for(let j=0; j<rows.children.length; j++) {
//     rows.children.item(j).classList.remove('zero');
//   }
// }

function appearRowAll(rows) {
  for(let i=0; i<rows.length; i++) {
    const items = rows[i];
    console.log(items.children.length);
    for(let j=0; j<items.children.length; j++) {
      if (i!=0 || j!=0) {
        items.children.item(j).classList.remove('zero');
      }
    }
  }
  expandChangeLess(rows);
}

function expandChangeMore(rows) {
  const expandSpan = rows[0].children.item(0).children.item(1); // tr:1行目 -> td:部品名 -> span:
  console.log(expandSpan);
  expandSpan.textContent = 'expand_more';
}

function expandChangeLess(rows) {
  const expandSpan = rows[0].children.item(0).children.item(1); // tr:1行目 -> td:部品名 -> span:
  console.log(expandSpan);
  expandSpan.textContent = 'expand_less';
}

function checkedTotal() {
  const assem = document.getElementById('assem');
  if(assem.className.includes('hidden')) return;

  const tableBody = document.getElementById('assemtablebody');
  const warnNoPrice = document.getElementById('warn-noprice');
  warnNoPrice.classList.add('hidden');

  console.log(tableBody);
  var totalPrice = 0;
  for(let i=0; i<tableBody.children.length; i++) {
    const devRows = tableBody.children.item(i); // tr
    const checkButtonElement = devRows.children.item(1).children.item(0); // td:選択 -> input:ラジオボタン
    if (checkButtonElement.checked) {
      const valueElement = devRows.children.item(5); // td:価格
      var value = valueElement.textContent;
      if (value == '価格情報なし') {
        warnNoPrice.classList.remove('hidden');
        continue;
      }
      var val = parseInt(value.replaceAll('¥', '').replaceAll(' ', '').replaceAll(',', ''), 10);
      console.log(val);
      totalPrice += val;
    }
  }
  const totalPriceTxt = document.getElementById('totalprice');
  totalPriceTxt.textContent = '¥ ' + totalPrice.toLocaleString();

  determineConfigurableByFlag();
}

function determineConfigurableByFlag() {
  const tableBody = document.getElementById('assemtablebody');
  let flagObj = {};
  for(let i=0; i<tableBody.children.length; i++) {
    const devRows = tableBody.children.item(i);
    const partsName = devRows.className;
    const checkButtonElement = devRows.children.item(1).children.item(0); // td:選択 -> input:ラジオボタン
    if (checkButtonElement.checked) {
      const flag1 = devRows.children.item(7); // td:flag1
      const flag2 = devRows.children.item(8); // td:flag2
      flagObj[partsName] = {flag1: flag1.textContent, flag2: flag2.textContent};
    }
  }

  const warn = document.getElementById('warn');
  warn.innerText = "";
  const bit = 0b11111111;
  const topBit = 0b10000000;
  const caseFlag1 = ('PCケース' in flagObj) ? parseInt(flagObj['PCケース']['flag1']) : null;
  const psuFlag1 = ('電源' in flagObj) ? parseInt(flagObj['電源']['flag1']) : null;
  const motherFlag1 = ('マザーボード' in flagObj) ? parseInt(flagObj['マザーボード']['flag1']) : null;
  const cpuFlag1 = ('CPU' in flagObj) ? parseInt(flagObj['CPU']['flag1']) : null;
  const coolerFlag1 = ('CPUクーラー' in flagObj) ? parseInt(flagObj['CPUクーラー']['flag1']) : null;
  const videoFlag1 = ('グラフィックボード' in flagObj) ? parseInt(flagObj['グラフィックボード']['flag1']) : null;
  const motherFlag2 = ('マザーボード' in flagObj) ? parseInt(flagObj['マザーボード']['flag2']) : null;
  const cpuFlag2 = ('CPU' in flagObj) ? parseInt(flagObj['CPU']['flag2']) : null;
  const memoryFlag2 = ('メモリ' in flagObj) ? parseInt(flagObj['メモリ']['flag2']) : null;

  // Case vs PSU
  let shift = 0;
  if (!(caseFlag1 == null) && !(psuFlag1 == null)) { // case and psu exist
    const casePowStandard = (caseFlag1 >>> shift) & bit;
    const psuPowStandard = (psuFlag1 >>> shift) & bit;
    if ((casePowStandard & topBit) != 0) { // built-in PSU case but psu exist
      warn.innerText += 'ケースに電源が内蔵されているのに電源が選択されています\n';
    } else if ((casePowStandard & psuPowStandard) == 0) {
      if (casePowStandard != 0) { // ignore the check if power type of case is unknown
        warn.innerText += 'ケースと電源の規格が一致していません\n';
      }
    }
  }

  // Case vs Motherboard
  if (!(caseFlag1 == null) && !(motherFlag1 == null)) { // case and motherboard exist
    shift = 8;
    const caseFormStandard = (caseFlag1 >>> shift) & bit;
    const motherFormStandard = (motherFlag1 >>> shift) & bit;
    console.log(caseFormStandard & motherFormStandard);
    if ((caseFormStandard & motherFormStandard) == 0) {
      warn.innerText += 'ケースとマザーボードの規格が一致していません\n';
    }
  }

  // Case vs CPU Cooler
  if (!(caseFlag1 == null) && !(coolerFlag1 == null)) { // case and cooler exist
    shift = 24;
    const caseSize = (caseFlag1 >>> shift) & bit;
    let coolerSize = (coolerFlag1 >>> shift) & bit;
    if ((coolerSize & topBit) != 0) { // water-cooled radiator
      coolerSize &= ~topBit;
    }
    if ((coolerSize+1) >= caseSize) {
      warn.innerText += 'CPUクーラーがケースが収まらない可能性があります\n';
    }
  }

  // Motherboard vs CPU
  if (!(motherFlag2 == null) && !(cpuFlag2 == null)) { // motherboard and cpu exist
    // Intel
    shift = 0;
    let motherSocket = (motherFlag2 >>> shift) & bit;
    let cpuSocket = (cpuFlag2 >>> shift) & bit;
    if ((motherSocket & cpuSocket) == 0) {
      // AMD
      shift = 8;
      motherSocket = (motherFlag2 >>> shift) & bit;
      cpuSocket = (cpuFlag2 >>> shift) & bit;
      if ((motherSocket & cpuSocket) == 0) {
        warn.innerText += 'マザーボードとCPUのソケットが一致していません\n';
      }
    }
  }

  // Motherboard vs Memory
  if (!(motherFlag2 == null) && !(memoryFlag2 == null)) { // motherboard and memory exist
    shift = 16;
    let isNotMatch = false;
    let motherType = (motherFlag2 >>> shift) & bit;
    let memoryType = (memoryFlag2 >>> shift) & bit;
    if ((motherType & topBit) != 0) { // mother S.O.DIMM ?
      if ((memoryType & topBit) != 0) { // memory S.O.DIMM ?
        motherType &= ~topBit;
        memoryType &= ~topBit;
        if ((motherType & memoryType) == 0) {
          isNotMatch = true;
        }
      } else {
        isNotMatch = true;
      }
    } else {
      if ((memoryType & topBit) == 0) { // memory not S.O.DIMM ?
        if ((motherType & memoryType) == 0) {
          isNotMatch = true;
        }
      } else {
        isNotMatch = true;
      }
    }
    if (isNotMatch) {
      warn.innerText += 'マザーボードとメモリの型が一致していません\n';
    }
  }

  // Power capacity check
  if (!(psuFlag1 == null)) {
    shift = 16;
    let capacity = (psuFlag1 >>> shift) & bit;
    capacity -= (cpuFlag1 >>> shift) & bit;
    capacity -= (videoFlag1 >>> shift) & bit;
    if (capacity < 0) {
      warn.innerText += '電源容量不足の可能性があります\n';
    }
  }

}