git clone https://github.com/tuwarij/Astrobee_Kobiko     //เป็นการก้อปปี้โปรเจคจำลองมาจากโปรเจคหลักโดยเราจะสามารถเปลี่ยนแปลงไฟล์ในโปรเจคจำลองนี้ได้โดยที่ไม่กระทบโปรเจคหลัก(ไฟล์จำลองที่เราทำการเปลี่ยนแปลงจะเรียกว่า local repository และไฟล์หลักที่อยู่คนละที่จะเรียกว่า remote repository);


git pull   //เป็นการดึงไฟล์ที่มีการเปลี่ยนแปลงใน remote มาเปลี่ยนแปลงใน local ถ้ามีการเปลี่ยนแปลงแก้ไขในlocalต้องcommitก่อน ถึงจะpullได้

--------------------------------------------------------------------------------------------------------------------

git add .   //เพิ่มไฟล์เข้าไปใน stage (. คือเพิ่มทั้งหมด ,หรือแทนเป็นชื่อไฟล์ ถ้าอยากaddทีละอัน) 

git commit -m “change function b”    //ยืนยันการเปลี่ยนแปลงไฟล์ที่ถูก add ลงใน stage

git push origin branchName //เป็นการส่งไฟล์ที่ commit แล้วเข้าสู่ remote repository

---------------------------------------------------------------------------------------------------------------------
branch ที่มีเป็น origin/branchName หมายถึงสร้างการอ้างถึง branch ใน remote(สร้างที่local) ,ส่วนbranchName เฉยๆคือสาขาที่คุณสร้างขึ้นเพื่อทำงานที่แยกออกจากสาขาหลัก (สร้างที่github)

git branch   //ดู branchที่มีอยู่   เครื่องหมายดอกจัน * หน้า หมายถึงเราทำงานที่brachนี้อยู่

git branch my_new_branch    //สร้าง brach ขึ้นมา (ตามหลังด้วยชื่อbranch ที่ต้องการสร้าง)

git checkout my_new_branch  //

-------------------------------

step
1. git clone https://github.com/tuwarij/Astrobee_Kobiko

2. ตอนนี้ถ้าเปิดงานที่cloneมา branch ที่เราอยู่ตอนนี้จะเป็น main

2.1 (optional ) ถ้าต้องการทำงานในbranchตัวเองให้สร้างbranch มา

3. เขียนโค้ดเอาmethod มาใส่ได้เลย

4. git add . 

5. git commit -m “งานที่ตัวเองทำลงไป”

6. git push   (อันนี้ในmain)(อยู่branchจะpushขึ้นmainใช้ git push origin branchName)