# **WTIH US**

---

## **Outline**

---

현재 휴대폰 갤러리를 보면, 원하는 사진을 찾기가 굉장히 어렵습니다.

즐겨찾기 기능이 있다 한들, 그때 무슨 일이 있었으며 누구와 함께 했다는 기록을 남기지 못합니다.

**WITHUS**는 개인 앨범을 제공함과 동시에, 메모 기능과 인원 태그 기능을 제공합니다.

## **Team**

---

김진영

[KimJinYeongZ - Overview](https://github.com/KimJinYeongZ)

이은영

[eunyoung-1118 - Overview](https://github.com/eunyoung-1118)

---

## **Tech Stack**

---

**Front-end** : Kotlin

**IDE** : Android Studio

---

## View

---

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/0d5c6c03-e149-4f2c-b3bc-baeb78bd6268" alt="앱 로딩화면" width="300">

앱 로딩화면

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/539a1744-b15a-4727-8578-1367fa019c1a" alt="연락처 탭 리스트" width="300">

연락처 탭 리스트

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/2bba0452-50bd-44eb-9399-dcf602b71c98" alt="연락처 추가 및 업데이트" width="300">

연락처 추가 및 업데이트

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/2e612436-39d7-4391-b39b-d37d3ed501bf" alt="연락처 프로필 및 전화 문자 연결" width="300">

연락처 프로필 및 전화 문자 연결

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/b8fb1d98-c1ce-46e3-ad12-b14032efcf4a" alt="연락처 검색" width="300">

연락처 검색

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/c13b0f70-290d-4e49-bec9-b2f9e6f68947" alt="사진 삭제" width="300">

사진 삭제

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/692351f7-a542-472b-9e3a-6783271fadab" alt="앨범 사진 세부사항" width="300">

앨범 사진 세부사항

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/63e0de33-3b19-41dd-a2b0-ee34b3371c37" alt="앨범 탭 사진 추가" width="300">

앨범 탭 사진 추가

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/e6f72028-c5fb-4a1b-91d4-134216811325" alt="사진 줌" width="300">

사진 줌

<img src="https://github.com/eunyoung-1118/MadCamp_Week1/assets/137619133/2f6aef89-ebe4-4bd6-8a6d-1f7d4d62c2b6" alt="지도 탭 마커 이동 및 클릭" width="300">

지도 탭 마커 이동 및 클릭
---

## **Detail**

---

- **데이터 저장**은 `RoomDB` 라이브러리를 이용합니다.
- **Page 1**
    - **연락처 동기화 및 목록**
        - **Update** 버튼을 클릭하면, `loadContacts()` 함수가 실행 되며, 실제 연락처의 이름, 번호, 사진을 `Cursor`를 활용하여 하나의 엔티티로 구성합니다.
        - 구성된 엔티티는 중복 확인 후, `PhoneBookRepository`에 `insert()` 함수를 통해 저장됩니다.
        - 모든 연락처가 저장된 후, `updateData(sortedList)`함수를 호출하여 `phoneBookAdpater`가 UI 재렌더링하도록 요청합니다.
        - `PhoneBookRepository`는 업데이트된 연락처의 목록을 갖습니다. 또한 렌더링 된 UI는 연락처의 최신정보를 갖습니다.
    - **연락처 프로필**
        - `ListView` 에 `setOnItemClickListener` 이벤트를 설정합니다.
        - 연락처를 클릭 한다면 `phoneProfileFragment`가 열리도록 하며, 탭이 제공될 때 클릭 당한 연락처의 고유 `id`도 전달됩니다.
        - 전달된 `id`를 통해  `PhoneBookRepository`에서 해당 `id`의 이름, 번호, 사진을 가져오며 탭을 렌더링합니다.
        - 프로필에는 전화 버튼과 문자 버튼이 존재합니다. 이 또한 `setOnClickListener`를 설정해두어 실제 전화 또는 문자로 연결되도록 합니다.
    - **연락처 검색**
        - 돋보기를 누르면, 연락처 검색 기능을 사용할 수 있습니다.
        - 이는 `contains` 함수를 활용해 기존의 `List`에서 `filteredList`를 만들어 화면을 다시 렌더링합니다.

- **Page 2**
    - **사진 추가하기**
        - **add** 버튼을 클릭하면, 갤러리에서 원하는 사진을 클릭하여 원하는 사진을 추가할 수 있습니다.
        - 추억이 기록된 사진들은 `Recyclerview`를 사용해 목록으로 나타냅니다.
    - **사진 삭제하기**
        - 사진마다 `setOnLongClickListener` 을 등록하여 기본 클릭보다 오래 유지 시, 사진 삭제가 가능합니다.
    - **추억 기록하기**
        - 사진
            - 사진을 클릭하면 `ZoomActivity`가 실행되어 전체화면에 사진을 나타냅니다.
        - 날짜
            - `ImageDetail` 엔티티를 생성할 당시, 날짜는 시간의 메타 데이터의 정보를 읽어들여 해당 엔티티에 저장합니다.
        - 장소
            - 구글 맵이 해석할 수 있는 주소 및 장소를 직접 기록합니다.
        - 인원
            - ‘+’ 버튼을 누르면 연락처에 있는 사람들의 목록이 나타납니다. `PhoneBookRepository`의 `getAllContact()` 함수를 사용하여 모든 연락처의 이름을 가져와 리스트로 저장합니다. 해당 리스트를 AlertDialog로 표현하여 , 목록에 있는 사람들 중 함께한 사람들을 클릭한 뒤 OK 버튼을 클릭하면, 함께했던 인원이 추가됩니다.
                - 함께했던 인원을 Button 요소로 만들어 클릭 시 해당 인원의  Page 1에 존재하는 Profile로 이동합니다.
            - `PhoneBookRepository`의 `getAllContact()` 함수를 사용하여 모든 연락처를 가져와 이름을 추출해 리스트로 저장합니다. 해당 리스트와 `AlertDialog`를 사용하여, 목록을 나타냅니다.
        - 있었던 일
            - 있었던 일을 텍스트로 직접 기록할 수 있습니다.
            - `maxLines`를 3으로 설정해 최대 3줄까지 기록할 수 있습니다.
        - 추억을 기록한 뒤 **Save** 버튼을 누르면, ‘사진, 날짜, 장소, 인원, 있었던 일’을 하나의 엔티티로 구성하여 `ImageDetailRepository`에 저장합니다.

- **Page 3**
    - **Marker 자동 추가**
        - Page3으로 이동될 때, `ImageDetailRepository`의 `getAllImages()`함수를 통해 모든 엔티티를 가져옵니다.
        - 반복문을 통해 해당 엔티티의 주소가 유효한 주소라면, Marker를 찍습니다.
        - Marker 이동 기능을 위해 Marker에 보이지 않는 번호를 표시합니다.
        - Marker 클릭 기능을 위해 Marker에 `setOnClickListener`를 설정합니다.
    - **Marker 이동**
        - Page 3 하단에는 **Next Marker** 버튼이 존재합니다.
        - 버튼을 클릭하면 다음 Marker의 위치를 파악 후, 해당 Marke로 포커스합니다.
    - **Marker 클릭**
        - 해당 Marker를 클릭 한다면, 미리 저장된 사진 url을 가지고, Page 2의 해당 이미지의 추억 탭으로 이동합니다.
        

---

## **Distribution**

---

https://drive.google.com/file/d/1QpdHxQZ_lV-aTL6cnBkH3kWlwiCd15FZ/view?usp=sharing
