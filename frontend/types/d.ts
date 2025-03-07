declare global {
    interface Window {
        daum: {
            Postcode: new (options: {
                oncomplete: (data: AddressData) => void;
                onresize?: (size: { width: number; height: number }) => void;
            }) => void;
        };
    }
}

export interface AddressData {
    zonecode: string;
    address: string;
    buildingName?: string;
}