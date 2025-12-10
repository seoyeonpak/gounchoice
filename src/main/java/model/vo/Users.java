package model.vo;

public class Users {
	private int userId; // user_id
	private String name; // name
	private String email; // email
	private String password; // password
	private String phoneNumber; // phone_number
	private String address; // address

	public Users() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Users(int userId, String name, String email, String password, String phoneNumber, String address) {
		super();
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.address = address;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Users [userId=" + userId + ", name=" + name + ", email=" + email + ", password=" + password
				+ ", phoneNumber=" + phoneNumber + ", address=" + address + "]";
	}

}