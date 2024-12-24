import java.util.*;

// Shipment (Gönderi) sınıfı
// Zaman Karmaşıklığı Notu: Sınıfın oluştuırulması O(1),
// Uzay Karmaşıklığı : Her Shipment nesnesi, sabit sayıda alan tuttuğu için O(1).
class Shipment
{
    String shipmentID;
    String date;
    boolean delivered;
    int deliveryTime; // gün olarak -> rota ağacındaki derinlikle belirlenir
    String destinationCity;

    public Shipment(String shipmentID, String date, boolean delivered, int deliveryTime, String destinationCity)
    {
        this.shipmentID = shipmentID;
        this.date = date;
        this.delivered = delivered;
        this.deliveryTime = deliveryTime;
        this.destinationCity = destinationCity;
    }
}

// Linked list için düğüm (Müşterinin gönderim geçmişi)
class ShipmentNode
{
    Shipment shipment;
    ShipmentNode next;
    public ShipmentNode(Shipment s) {
        this.shipment = s;
        this.next = null;
    }
}

// Müşteri sınıfı
// LinkedList ve Stack kullanımı
class Customer {
    String customerID;
    String name;
    // Müşterinin gönderim geçmişi linked list ile
    ShipmentNode shipmentHead;
    // Müşterinin son 5 gönderimi için stack
    Stack<Shipment> lastFiveStack;

    public Customer(String id, String name) {
        this.customerID = id;
        this.name = name;
        this.shipmentHead = null;
        this.lastFiveStack = new Stack<>();
    }

    // Gönderiyi tarih sırasına göre linked list'e ekler
    // Zaman karmaşıklığı: O(n) (n = gönderi sayısı)
    // Linked list üzerinde ekleme sırasında doğru yeri bulmak için ilerliyoruz
    // Uzay karmaşıklığı: O(1) ek alan (yeni node sabit alan tutar)
    public void addShipmentSorted(Shipment s) {
        ShipmentNode newNode = new ShipmentNode(s);
        if(shipmentHead == null) {
            shipmentHead = newNode;
        } else {
            ShipmentNode current = shipmentHead;
            ShipmentNode prev = null;
            // Tarihe göre sıralama -> "tarih" karşılaştırılıyor (YYYYMMDD format).
            while(current != null && current.shipment.date.compareTo(s.date) < 0) {
                prev = current;
                current = current.next;
            }
            if(prev == null) {
                // Başa ekleme
                newNode.next = shipmentHead;
                shipmentHead = newNode;
            } else {
                prev.next = newNode;
                newNode.next = current;
            }
        }

        // Stack'e push (son gönderileri takip)
        // Zaman karmaşıklığı: O(1) push
        // Uzay karmaşıklığı: O(1)
        if(lastFiveStack.size()<5) {
            lastFiveStack.push(s);
        } else {
            // 5 sınırını aşmamak için en eskiyi atar
            // Stack LIFO olduğundan en alttaki eleman en eski. Onu atmak bi tık zor
            // çözüm: geçici liste yaklaşımı
            List<Shipment> tempList = new ArrayList<>(lastFiveStack);
            // tempList.get(0) => en eski
            tempList.remove(0);
            tempList.add(s);
            lastFiveStack.clear();
            for(Shipment sh: tempList) {
                lastFiveStack.push(sh);
            }
        }
    }

    // Son 5 gönderiyi görüntüleyecek
    // Zaman karmaşıklığı: O(k), k = 5 sabit => O(1)
    public void printLastFiveShipments() {
        if(lastFiveStack.isEmpty()) {
            System.out.println("Gönderim geçmişi boş!");
            return;
        }
        System.out.println("Son Gönderiler (en yeni en üstte):");
        for (int i = lastFiveStack.size()-1; i>=0; i--) {
            Shipment sh = lastFiveStack.get(i);
            System.out.println("ID: " + sh.shipmentID
                    + ", Tarih: " + sh.date
                    + ", Teslim: " + (sh.delivered?"Evet":"Hayir")
                    + ", Şehir: " + sh.destinationCity);
        }
    }

    // Tüm gönderim geçmişini yazdır
    // Zaman karmaşıklığı: O(n) (n = gönderi sayısı)
    public void printAllShipments() {
        ShipmentNode current = shipmentHead;
        while(current != null) {
            Shipment sh = current.shipment;
            System.out.println("ID: " + sh.shipmentID
                    + ", Tarih: " + sh.date
                    + ", Teslim: " + (sh.delivered?"Evet":"Hayir")
                    + ", Süre(gün): " + sh.deliveryTime
                    + ", Şehir: " + sh.destinationCity);
            current = current.next;
        }
    }
}

// Müşteri LinkedList yapısı
class CustomerNode {
    Customer customer;
    CustomerNode next;
    public CustomerNode(Customer c) {
        this.customer = c;
        this.next = null;
    }
}

class CustomerList {
    CustomerNode head;

    // Yeni müşteri ekleme
    // Zaman karmaşıklığı: O(n) (listenin sonuna ekleme)
    // Uzay karmaşıklığı: O(1)
    public void addCustomer(Customer c) {
        if(head == null) {
            head = new CustomerNode(c);
        } else {
            CustomerNode current = head;
            while(current.next != null) {
                current = current.next;
            }
            current.next = new CustomerNode(c);
        }
    }

    // ID ile müşteri bul
    // Zaman karmaşıklığı: O(n)
    public Customer findCustomerByID(String id) {
        CustomerNode current = head;
        while(current != null) {
            if(current.customer.customerID.equals(id)) return current.customer;
            current = current.next;
        }
        return null;
    }

    // Aynı ID var mı kontrolü
    // Zaman karmaşıklığı: O(n)
    public boolean existsCustomerID(String id) {
        CustomerNode current = head;
        while(current != null) {
            if(current.customer.customerID.equals(id)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
}

// PriorityQueue kullanarak kargo önceliklendirme
// Teslim süresine göre öncelik
// Ekleme (offer) O(log n), çıkarma (poll) O(log n), n = PQ’deki eleman sayısı
// Uzay karmaşıklığı: O(n)
class ShipmentComparator implements Comparator<Shipment> {
    @Override
    public int compare(Shipment s1, Shipment s2) {
        return Integer.compare(s1.deliveryTime, s2.deliveryTime);
    }
}

// Ağaç yapısı (Rotalar)
class RouteNode {
    String cityName;
    String cityID;
    ArrayList<RouteNode> children;

    public RouteNode(String cityName, String cityID) {
        this.cityName = cityName;
        this.cityID = cityID;
        this.children = new ArrayList<>();
    }

    public void addChild(RouteNode node) {
        children.add(node);
    }
}

class RouteTree {
    RouteNode root;

    public RouteTree(String rootName, String rootID) {
        root = new RouteNode(rootName, rootID);
    }

    // Ağacı yazdırma - basit DFS
    // Zaman karmaşıklığı: O(n) (tüm düğümleri dolaşır),
    // Uzay karmaşıklığı: O(h) (derinlik kadar recursion stack), h = ağaç yüksekliği
    public void printTree(RouteNode node, String indent) {
        System.out.println(indent + node.cityName + " (" + node.cityID + ")");
        for(RouteNode child : node.children) {
            printTree(child, indent + "--");
        }
    }
    public void printTree() {
        printTree(root, "");
    }

    // Şehir adını bulup derinliğini döndür (root’un derinliği 0)
    // Zaman karmaşıklığı: O(n) (tüm ağaç düğümlerini ziyaret edebilir)
    // Uzay karmaşıklığı: O(h)
    public int getCityDepth(String cityName) {
        return getCityDepthDFS(root, cityName, 0);
    }

    // DFS ile derinlik bulma
    private int getCityDepthDFS(RouteNode current, String cityName, int depth) {
        if(current.cityName.equalsIgnoreCase(cityName)) {
            return depth;
        }
        for(RouteNode child : current.children) {
            int result = getCityDepthDFS(child, cityName, depth + 1);
            if(result != -1) {
                return result;
            }
        }
        return -1;
    }

    // Şehir ağaçta var mı?
    public boolean cityExists(String cityName) {
        return getCityDepth(cityName) != -1;
    }
}

// Sorting & Searching
class SortAndSearch {
    // Merge Sort (teslim edilmeyen kargolar için)
    // Zaman karmaşıklığı: O(n log n)
    // Uzay karmaşıklığı: O(n) -> yardimci listeler
    public static void mergeSort(ArrayList<Shipment> arr) {
        if (arr.size() <= 1) return;
        int mid = arr.size()/2;
        ArrayList<Shipment> left = new ArrayList<>(arr.subList(0, mid));
        ArrayList<Shipment> right = new ArrayList<>(arr.subList(mid, arr.size()));
        mergeSort(left);
        mergeSort(right);
        merge(arr, left, right);
    }

    private static void merge(ArrayList<Shipment> arr, ArrayList<Shipment> left, ArrayList<Shipment> right) {
        arr.clear();
        int i=0,j=0;
        while(i<left.size() && j<right.size()) {
            if(left.get(i).deliveryTime <= right.get(j).deliveryTime) {
                arr.add(left.get(i));
                i++;
            } else {
                arr.add(right.get(j));
                j++;
            }
        }
        while(i<left.size()) {
            arr.add(left.get(i)); i++;
        }
        while(j<right.size()) {
            arr.add(right.get(j)); j++;
        }
    }

    // Binary Search (teslim edilmiş kargolar ID'ye göre sıralı listede aranacak)
    // Zaman karmaşıklığı: O(log n)
    // Uzay karmaşıklığı: O(1)
    public static int binarySearchByID(ArrayList<Shipment> arr, String targetID) {
        int low=0;
        int high=arr.size()-1;
        while(low<=high) {
            int mid=(low+high)/2;
            int cmp = arr.get(mid).shipmentID.compareTo(targetID);
            if(cmp==0) return mid;
            else if(cmp<0) low=mid+1;
            else high=mid-1;
        }
        return -1;
    }
}

public class CargoSystem {
    static CustomerList customers = new CustomerList();
    static PriorityQueue<Shipment> shipmentPQ = new PriorityQueue<>(new ShipmentComparator());
    // Istanbul merkez
    static RouteTree routeTree = new RouteTree("Istanbul", "IST01");

    static Scanner sc = new Scanner(System.in);


    static String getValidNumericID(String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if(!input.matches("\\d+")) {
            System.out.println("Hata: ID sadece rakamlardan oluşmalı!");
            return null;
        }
        // Benzersiz kontrol -> eğer mevcutsa hata
        if(customers.existsCustomerID(input)) {
            System.out.println("Hata: Bu müşteri ID zaten kullanılıyor!");
            return null;
        }
        return input;
    }


    static String getValidName(String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if(!input.matches("[a-zA-ZÇçĞğİıÖöŞşÜü ]+")) {
            System.out.println("Hata: İsim sadece harflerden oluşmalı!");
            return null;
        }
        return input;
    }

    // Shipment ID
    static String getValidShipmentID(String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if(!input.matches("\\d+")) {
            System.out.println("Hata: Gönderi ID sadece rakamlardan oluşmalı!");
            return null;
        }
        return input;
    }

    // Tarih formatı kontrol -> YYYYMMDD (sadece rakam + basit takvim doğrulaması)
    // İleri seviye (ay/gün geçerlilik) eklenebilir
    static String getValidDate(String prompt) {
        System.out.print(prompt);
        String date = sc.nextLine().trim();
        // Temel kontrol: 8 haneli rakam
        if(!date.matches("\\d{8}")) {
            System.out.println("Hata: Tarih formatı YYYYMMDD olmalı ve rakamlardan oluşmalı!");
            return null;
        }
        // Daha ileri kontrol -> yıl=yyyy, ay=MM, gün=dd
        String yyyy = date.substring(0,4);
        String mm = date.substring(4,6);
        String dd = date.substring(6,8);
        // Ay 01-12 arası mı?
        int month;
        int day;
        try {
            month = Integer.parseInt(mm);
            day = Integer.parseInt(dd);
        } catch(Exception e) {
            System.out.println("Geçersiz tarih rakamları!");
            return null;
        }
        if(month<1 || month>12) {
            System.out.println("Geçersiz ay girişi!");
            return null;
        }
        // Gün kabaca 1-31 arası (aylara göre değişebiliyor, ama basit tutalım)
        if(day<1 || day>31) {
            System.out.println("Geçersiz gün girişi!");
            return null;
        }
        return date;
    }

    public static void main(String[] args) {
        // Marmara bölgesi rotaları:
        RouteNode bursa = new RouteNode("Bursa","BRS01");
        RouteNode edirne = new RouteNode("Edirne","EDR01");
        routeTree.root.addChild(bursa);
        routeTree.root.addChild(edirne);

        RouteNode kocaeli = new RouteNode("Kocaeli","KCL01");
        bursa.addChild(kocaeli);

        RouteNode tekirdag = new RouteNode("Tekirdag","TKD01");
        edirne.addChild(tekirdag);

        while(true) {
            System.out.println("\n--- Kargo Sistemi Menu ---");
            System.out.println("1. Yeni müşteri ekle");
            System.out.println("2. Müşteriye kargo gönderimi ekle");
            System.out.println("3. Kargo durumu sorgula (teslim edilmişlerde binary search)");
            System.out.println("4. Gönderim geçmişini görüntüle (Tüm geçmiş)");
            System.out.println("5. Son 5 gönderimi görüntüle (Stack)");
            System.out.println("6. Teslim edilmemiş kargoları teslim süresine göre sıralı listele (Merge Sort)");
            System.out.println("7. Teslimat rotalarını göster (Ağaç)");
            System.out.println("8. Kargo öncelik kuyruğuna ekle ve en öncelikli kargoyu işleme al");
            System.out.println("0. Çıkış");
            System.out.print("Seçim: ");
            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch(Exception e) { }

            if(choice == 0) {
                System.out.println("Program kapatılıyor...");
                break;
            }

            switch(choice) {
                case 1: {
                    String cid = getValidNumericID("Müşteri ID (sadece rakam): ");
                    if(cid == null) break; // hatalı veya tekrar menüye dön
                    String cname = getValidName("Müşteri Ad Soyad (sadece harf): ");
                    if(cname == null) break;
                    Customer newC = new Customer(cid, cname);
                    customers.addCustomer(newC);
                    System.out.println("Müşteri eklendi.");
                    break;
                }
                case 2: {
                    // Müşteriye kargo ekle
                    System.out.print("Müşteri ID: ");
                    String custID = sc.nextLine().trim();
                    Customer cust = customers.findCustomerByID(custID);
                    if(cust == null) {
                        System.out.println("Müşteri bulunamadı!");
                        break;
                    }
                    String sid = getValidShipmentID("Gönderi ID: ");
                    if(sid == null) break;

                    String date = getValidDate("Tarih (YYYYMMDD): ");
                    if(date == null) break;

                    System.out.print("Teslim edildi mi? (true/false): ");
                    boolean delivered = Boolean.parseBoolean(sc.nextLine().trim());

                    // Şehir bilgisi
                    System.out.print("Şehir Adı (Marmara bölgesi için): ");
                    String cityName = sc.nextLine().trim();
                    // Rota ağacında var mı?
                    if(!routeTree.cityExists(cityName)) {
                        System.out.println("Hata: Bu şehir rota ağacında bulunamadı!");
                        break;
                    }
                    // Derinlik => Teslimat süresi
                    int depth = routeTree.getCityDepth(cityName);
                    // Örnek: depth * 1 gün
                    int dtime = depth; // dilersen depth * 2 vs. yapabilirsin

                    Shipment sh = new Shipment(sid, date, delivered, dtime, cityName);
                    cust.addShipmentSorted(sh);
                    System.out.println("Kargo eklendi. Teslim süresi (otomatik): " + dtime + " gün.");
                    break;
                }
                case 3: {
                    // Teslim edilmişleri toplayalım
                    ArrayList<Shipment> deliveredList = new ArrayList<>();
                    CustomerNode cn = customers.head;
                    // Zaman karmaşıklığı: O(m) m = toplam müşteri * ortalama gönderi sayısı
                    while(cn != null) {
                        ShipmentNode sn = cn.customer.shipmentHead;
                        while(sn != null) {
                            if(sn.shipment.delivered) deliveredList.add(sn.shipment);
                            sn = sn.next;
                        }
                        cn = cn.next;
                    }
                    // Sıralama -> O(m log m)
                    deliveredList.sort( (a,b)->a.shipmentID.compareTo(b.shipmentID) );

                    String searchID = getValidShipmentID("Aranacak Gönderi ID (sadece rakam): ");
                    if(searchID == null) break;

                    // Binary search -> O(log m)
                    int idx = SortAndSearch.binarySearchByID(deliveredList, searchID);
                    if(idx>=0) {
                        System.out.println("Kargo bulundu: "
                                + deliveredList.get(idx).shipmentID
                                + " (Teslim Edildi), Şehir: "
                                + deliveredList.get(idx).destinationCity);
                    } else {
                        System.out.println("Kargo bulunamadı");
                    }
                    break;
                }
                case 4: {
                    System.out.print("Müşteri ID: ");
                    String cid4 = sc.nextLine().trim();
                    Customer c4 = customers.findCustomerByID(cid4);
                    if(c4==null) {
                        System.out.println("Müşteri yok!");
                        break;
                    }
                    c4.printAllShipments();
                    break;
                }
                case 5: {
                    System.out.print("Müşteri ID: ");
                    String cid5 = sc.nextLine().trim();
                    Customer c5 = customers.findCustomerByID(cid5);
                    if(c5==null) {
                        System.out.println("Müşteri yok!");
                        break;
                    }
                    c5.printLastFiveShipments();
                    break;
                }
                case 6: {
                    // Teslim edilmemiş kargoları topla
                    ArrayList<Shipment> undeliveredList = new ArrayList<>();
                    CustomerNode cn2 = customers.head;
                    while(cn2 != null) {
                        ShipmentNode sn2 = cn2.customer.shipmentHead;
                        while(sn2 != null) {
                            if(!sn2.shipment.delivered) undeliveredList.add(sn2.shipment);
                            sn2 = sn2.next;
                        }
                        cn2 = cn2.next;
                    }
                    // Merge sort -> O(k log k), k = teslim edilmemiş kargo sayısı
                    SortAndSearch.mergeSort(undeliveredList);
                    System.out.println("Teslim edilmemiş kargolar (Teslim süresine göre sıralı):");
                    for(Shipment s: undeliveredList) {
                        System.out.println("ID: " + s.shipmentID
                                + " Süre: " + s.deliveryTime
                                + " Tarih: " + s.date
                                + " Şehir: " + s.destinationCity);
                    }
                    break;
                }
                case 7:
                    System.out.println("Rota Ağacı (İstanbul merkez):");
                    routeTree.printTree();
                    break;
                case 8: {
                    String pqid = getValidShipmentID("Gönderi ID (sadece rakam): ");
                    if(pqid==null) break;

                    // Ağaçtaki herhangi bir şehir diyelim
                    // Örnek: Kulanıcıdan city alıp depth hesaplardık.
                    // Burada basitçe city = 'Bursa' vs. diyebiliriz.

                    System.out.print("Şehir Adı: ");
                    String cityName = sc.nextLine().trim();
                    if(!routeTree.cityExists(cityName)) {
                        System.out.println("Hata: Bu şehir rota ağacında bulunamadı!");
                        break;
                    }
                    int depth = routeTree.getCityDepth(cityName);

                    // delivered = false, date basit sabit
                    Shipment pqShip = new Shipment(pqid, "20240101", false, depth, cityName);
                    // PQ ekleme -> O(log n)
                    shipmentPQ.offer(pqShip);
                    System.out.println("Kargo öncelik kuyruğuna eklendi (Şehir: " + cityName + ", Süre: " + depth + ").");
                    System.out.println("En öncelikli kargo işleniyor...");
                    // poll -> O(log n)
                    Shipment top = shipmentPQ.poll();
                    if(top!=null) {
                        System.out.println("İşlenen kargo: " + top.shipmentID
                                + " Süre: " + top.deliveryTime
                                + " Şehir: " + top.destinationCity);
                    }
                    break;
                }
                default:
                    System.out.println("Geçersiz seçim!");
            }
        }
    }
}
