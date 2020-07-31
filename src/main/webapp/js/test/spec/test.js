/**
 *
 */


// include all JS files 
// (() => {
//     const directory = "../../";
//     const filenames = ["jquery-3""auth.js", "config.js", "captions.js"
// })();

describe('My First Test', () => {
    it('this better fail', () => {
        expect('yes').toEqual('no');
    });

    it('trying stuff', ()=> {
        expect(config.api_key).toEqual('yes');
    });
});
