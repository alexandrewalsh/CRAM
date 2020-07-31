/**
 * Tests related to time manipulation functions
 *
 * Testing strategy: test timestamps can be converted to seconds and vice versa for 
 *                   times containing 0+ hours, 0+ minutes, 0+ seconds
 */


describe('Test generic H:M:S', function() {
    // test a variety of single-double digit numbers
    it('Test missing hours', () => {
        expect(timestampToEpoch("0:01:43")).toEqual(103);
        expect(timestampToEpoch("00:01:43")).toEqual(103);
        expect(timestampToEpoch("00:1:43")).toEqual(103);
        expect(timestampToEpoch("0:1:43")).toEqual(103);
        expect(timestampToEpoch("01:1:43")).not.toEqual(103);
    });

    it('Test missing seconds', () => {
        expect(timestampToEpoch("2:30:00")).toEqual(9000);
        expect(timestampToEpoch("2:30:0")).toEqual(9000);
    });

    it('Test missing minutes', () => {
        expect(timestampToEpoch("2:00:03")).toEqual(7203);
    })

    it('Test H:M:S', () => {
        expect(timestampToEpoch("1:52:34")).toEqual(6754);
        expect(timestampToEpoch("01:52:34")).toEqual(6754);
    });
});

describe('Test generic M:S', function() {
    // test a variety of single-double digit numbers
    it('Test M:S', () => {
        expect(timestampToEpoch("01:43")).toEqual(103);
        expect(timestampToEpoch("1:43")).toEqual(103);
    });

    it('Test missing seconds', () => {
        expect(timestampToEpoch("30:00")).toEqual(1800);
        expect(timestampToEpoch("30:0")).toEqual(1800);
    });

    it('Test missing minutes', () => {
        expect(timestampToEpoch("00:03")).toEqual(3);
        expect(timestampToEpoch("0:3")).toEqual(3);
    })
});

describe("Test reflective conversions", function() {
    it('Test H:M:S -> Epoch -> H:M:S', ()=>{
        expect(epochToTimestamp(timestampToEpoch("1:52:34"))).toEqual("01:52:34");
        expect(epochToTimestamp(timestampToEpoch("0:43:23"))).toEqual("43:23");
        expect(epochToTimestamp(timestampToEpoch("1:4:7"))).toEqual("01:04:07");
        expect(epochToTimestamp(timestampToEpoch("53:00:00"))).toEqual("53:00:00");
    });

    it('Test Epoch -> H:M:S -> Epoch', ()=>{
        expect(timestampToEpoch(epochToTimestamp(103))).toEqual(103);
        expect(timestampToEpoch(epochToTimestamp(0))).toEqual(0);
        expect(timestampToEpoch(epochToTimestamp(9687654321))).toEqual(9687654321);
    });
});
