try:
    from passlib.context import CryptContext
    print("Successfully imported CryptContext")
    pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
    print("Successfully created CryptContext")
except Exception as e:
    print(f"An error occurred: {e}")
